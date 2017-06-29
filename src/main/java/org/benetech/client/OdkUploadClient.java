package org.benetech.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.benetech.constants.GeneralConsts;
import org.benetech.security.SecurityUtils;
import org.opendatakit.api.forms.entity.FormUploadResult;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The OdkUploadClient code is separated from OdkClient into its own class, because it uses the
 * underlying HttpClient instead of RestTemplate. We ran into a number of "Connection reset" and
 * "Broken pipe" errors using the RestTemplate to upload files larger than about 2 MB, and
 * HttpClient allows us more leeway in adjusting configuration options.
 * 
 * The most notable difference from OdkClient that we are doing pre-emptive digest 
 * authentication, using an empty post to get the www-auth challenge header instead of the post 
 * containing the form.  Then we use that header to create the credentials to upload the form.
 * 
 * @author Caden Howell <cadenh@benetech.org>
 *
 */
public class OdkUploadClient {

  public static final String FORM_UPLOAD_ENDPOINT = "/forms/{appId}/{odkClientVersion}";

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final Log logger = LogFactory.getLog(OdkUploadClient.class);

  private static final String NONCE_REGEX = ".*nonce=\"([^\"]*)\".*";

  private static final Pattern NONCE_PATTERN =
      Pattern.compile(NONCE_REGEX, Pattern.CASE_INSENSITIVE);
  
  private OdkClient odkClient;

  public OdkUploadClient(OdkClient odkClient) {
    this.odkClient = odkClient;
  }

  /**
   * Upload multipart form containing a new survey to web service. For this endpoint, we are
   * preempting digest authentication by using an empty POST call to get the www-authentication header.
   * 
   * @param file
   * @param offices
   * @return
   * @throws IOException
   */
  public FormUploadResult uploadFile(final MultipartFile file,
      final List<String> offices) throws IOException {

    FormUploadResult formUploadResult = null;

    final String postUploadEndpoint = odkClient.getUrl(FORM_UPLOAD_ENDPOINT);
    final URL postUploadUrl = new URL(postUploadEndpoint);
    final HttpHost target =
        new HttpHost(postUploadUrl.getHost(), postUploadUrl.getPort(), postUploadUrl.getProtocol());

    final CloseableHttpClient preemptiveClient = getPreemptiveClient(postUploadUrl);
    final HttpClientContext clientContext = getPreemptiveContext(target, postUploadEndpoint);

    final HttpEntity requestEntity = buildRequestEntity(offices, file);

    final HttpPost httpPost = new HttpPost(postUploadEndpoint);
    httpPost.setEntity(requestEntity);
    try {
      final CloseableHttpResponse response = preemptiveClient.execute(target, httpPost, clientContext);
      final HttpEntity responseEntity = response.getEntity();
      formUploadResult = mapper.readValue(responseEntity.getContent(), FormUploadResult.class);
      EntityUtils.consume(responseEntity);
    } finally {
      preemptiveClient.close();
    }
    return formUploadResult;
  }

  /**
   * Build the multipart/form-data request
   * 
   * @param offices List of regional offices
   * @param file Zipped file containing survey definition
   * @return
   * @throws IOException
   * @see http://hc.apache.org/httpcomponents-client-4.3.x/httpmime/examples/org/apache/http/examples/entity/mime/ClientMultipartFormPost.java
   */
  private static HttpEntity buildRequestEntity(final List<String> offices, final MultipartFile file)
      throws IOException {
    final MultipartEntityBuilder builder = MultipartEntityBuilder.create();

    for (final String office : offices) {
      builder.addTextBody(GeneralConsts.OFFICE_ID, office, ContentType.TEXT_PLAIN);
    }

    builder.addBinaryBody(GeneralConsts.ZIP_FILE, file.getBytes(),
        ContentType.create("application/zip"), file.getOriginalFilename());
    final HttpEntity requestEntity = builder.build();
    return requestEntity;
  }

  /**
   * Set up a pre-emptive client that sends Digest Authentication header without challenge
   * 
   * @param postUploadUrl
   * @return
   */
  private static CloseableHttpClient getPreemptiveClient(final URL postUploadUrl) {

    final SecurityContext securityContext = SecurityContextHolder.getContext();
    @SuppressWarnings("unchecked")
    final Map<String, Object> userDetails = (Map<String, Object>) securityContext.getAuthentication().getDetails();
    
    if (userDetails == null || userDetails.get(GeneralConsts.PREEMPTIVE_CREDENTIALS) == null) {
      SecurityUtils.logout();
      
      throw new PreAuthenticatedCredentialsNotFoundException("Cannot find credentials needed for file download.  Please logout and log in again.");
    } 

    final UsernamePasswordCredentials preemptiveCredentials = (UsernamePasswordCredentials)userDetails.get(GeneralConsts.PREEMPTIVE_CREDENTIALS);

    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    credentialsProvider.setCredentials(
        new AuthScope(postUploadUrl.getHost(), postUploadUrl.getPort()),
        preemptiveCredentials);

    final CloseableHttpClient preemptiveClient =
        HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();

    return preemptiveClient;
  }

  /**
   * Set up a pre-emptive context that sends Digest Authentication header without challenge
   * 
   * @param postUploadUrl
   * @return
   * @see https://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples/client/ClientPreemptiveDigestAuthentication.java
   */
  private HttpClientContext getPreemptiveContext(HttpHost target,
      String postUploadEndpoint) throws ClientProtocolException, IOException {

    final String nonce = getNonce(target, postUploadEndpoint);
    
    final AuthCache authCache = new BasicAuthCache();
    final DigestScheme digestAuth = new DigestScheme();
    digestAuth.overrideParamter("realm", odkClient.getOdkRealm());
    digestAuth.overrideParamter("nonce", nonce);
    authCache.put(target, digestAuth);
    
    final HttpClientContext localContext = HttpClientContext.create();
    localContext.setAuthCache(authCache);
    
    final RequestConfig config = RequestConfig.custom().setExpectContinueEnabled(true).build();
    localContext.setRequestConfig(config);

    return localContext;
  }

  /**
   * Use a "HEAD" call to the current user endpoint to get the nonce
   * 
   * @param restTemplate
   * @param odkClient
   * @return
   * @throws ClientProtocolException
   * @throws IOException
   * @throws URISyntaxException
   */
  private static String getNonce(final HttpHost target, final String postUri)
      throws ClientProtocolException, IOException {

    final CloseableHttpClient httpClient = HttpClients.custom().build();
    Header header = null;

    final HttpPost httpPost;
    try {
      httpPost = new HttpPost(new URI(postUri));
      final HttpResponse headResponse = httpClient.execute(target, httpPost);
      header = headResponse.getFirstHeader(AUTH.WWW_AUTH);

    } catch (URISyntaxException e) {
      logger.error("Unable to get nonce for file upload pre-emptive authentication", e);
    }
    return header != null ? parseNonce(header.getValue()) : null;
  }

  static String parseNonce(String header) {
    String nonce = null;
    final Matcher matcher = NONCE_PATTERN.matcher(header);
    if (matcher.matches()) {
      nonce = matcher.group(1);
    }
    return nonce;
  }
}
