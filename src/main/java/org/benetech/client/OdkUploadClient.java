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


  /**
   * Upload multipart form containing a new survey to web service. For this one endpoint, we are
   * preempting digest authentication by using a HEAD call to get the www-authentication header.
   * 
   * @param file
   * @param offices
   * @return
   * @throws IOException
   * 
   */
  public static FormUploadResult uploadFile(OdkClient odkClient, MultipartFile file,
      List<String> offices) throws IOException {
    


    FormUploadResult formUploadResult = null;

    String postUploadEndpoint = odkClient.getUrl(FORM_UPLOAD_ENDPOINT);
    URL postUploadUrl = new URL(postUploadEndpoint);
    HttpHost target =
        new HttpHost(postUploadUrl.getHost(), postUploadUrl.getPort(), postUploadUrl.getProtocol());

    CloseableHttpClient preemptiveClient = getPreemptiveClient(postUploadUrl);
    HttpClientContext clientContext = getPreemptiveContext(odkClient, target, postUploadEndpoint);

    HttpEntity requestEntity = buildRequestEntity(offices, file);

    HttpPost httpPost = new HttpPost(postUploadEndpoint);
    httpPost.setEntity(requestEntity);
    try {
      CloseableHttpResponse response = preemptiveClient.execute(target, httpPost, clientContext);
      HttpEntity responseEntity = response.getEntity();
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
  static HttpEntity buildRequestEntity(List<String> offices, MultipartFile file)
      throws IOException {
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

    for (String office : offices) {
      builder.addTextBody(GeneralConsts.OFFICE_ID, office, ContentType.TEXT_PLAIN);
    }

    builder.addBinaryBody(GeneralConsts.ZIP_FILE, file.getBytes(),
        ContentType.create("application/zip"), file.getOriginalFilename());
    HttpEntity requestEntity = builder.build();
    return requestEntity;

  }

  /**
   * Set up a pre-emptive client that sends Digest Authentication header without challenge
   * 
   * @param postUploadUrl
   * @return
   */
  static CloseableHttpClient getPreemptiveClient(URL postUploadUrl) {

    SecurityContext securityContext = SecurityContextHolder.getContext();
    Map<String, Object> userDetails = (Map<String, Object>) securityContext.getAuthentication().getDetails();
    
    if (userDetails == null || userDetails.get(GeneralConsts.PREEMPTIVE_CREDENTIALS) == null) {
      SecurityUtils.logout();
      
      throw new PreAuthenticatedCredentialsNotFoundException("Cannot find credentials needed for file download.  Please logout and log in again.");
    } 

    UsernamePasswordCredentials preemptiveCredentials = (UsernamePasswordCredentials)userDetails.get(GeneralConsts.PREEMPTIVE_CREDENTIALS);

    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    credentialsProvider.setCredentials(
        new AuthScope(postUploadUrl.getHost(), postUploadUrl.getPort()),
        preemptiveCredentials);

    CloseableHttpClient preemptiveClient =
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
  static HttpClientContext getPreemptiveContext(OdkClient odkClient, HttpHost target,
      String postUploadEndpoint) throws ClientProtocolException, IOException {

    String nonce = getNonce(target, postUploadEndpoint);

    // Create AuthCache instance
    AuthCache authCache = new BasicAuthCache();
    // Generate DIGEST scheme object, initialize it and add it to the local
    // auth cache
    DigestScheme digestAuth = new DigestScheme();
    // Suppose we already know the realm name
    digestAuth.overrideParamter("realm", odkClient.getOdkRealm());
    // Suppose we already know the expected nonce value
    digestAuth.overrideParamter("nonce", nonce);
    authCache.put(target, digestAuth);



    // Add AuthCache to the execution context
    HttpClientContext localContext = HttpClientContext.create();
    localContext.setAuthCache(authCache);

    logger.info("Setting request config to expect continue enabled");
    RequestConfig config = RequestConfig.custom().setExpectContinueEnabled(true).build();
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
  static String getNonce(HttpHost target, String postUri)
      throws ClientProtocolException, IOException {

    CloseableHttpClient httpClient = HttpClients.custom().build();
    Header header = null;

    HttpPost httpPost;
    try {
      httpPost = new HttpPost(new URI(postUri));
      HttpResponse headResponse = httpClient.execute(target, httpPost);
      header = headResponse.getFirstHeader(AUTH.WWW_AUTH);

    } catch (URISyntaxException e) {
      logger.error("Unable to get nonce for file upload pre-emptive authentication", e);
    }
    return header != null ? parseNonce(header.getValue()) : null;
  }

  static String parseNonce(String header) {
    String nonce = null;
    Matcher matcher = NONCE_PATTERN.matcher(header);
    if (matcher.matches()) {
      nonce = matcher.group(1);
    }
    return nonce;
  }
}
