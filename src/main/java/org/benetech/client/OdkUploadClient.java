package org.benetech.client;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.benetech.constants.GeneralConsts;
import org.benetech.security.client.digest.HttpComponentsClientHttpRequestFactoryDigestAuth;
import org.opendatakit.api.forms.entity.FormUploadResult;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The OdkUploadClient code is separated from OdkClient into its own class, because it uses the
 * underlying HttpClient instead of RestTemplate.
 * We ran into a number of "Connection reset" and "Broken pipe" errors using the RestTemplate to upload
 * files larger than about 2 MB, and HttpClient allows us more leeway in adjusting configuration options.
 * 
 * @author Caden Howell <cadenh@benetech.org>
 *
 */
public class OdkUploadClient {

  public static String FORM_UPLOAD_ENDPOINT = "/forms/{appId}/{odkClientVersion}";

  private static ObjectMapper mapper = new ObjectMapper();

  private static Log logger = LogFactory.getLog(OdkUploadClient.class);

  /**
   * Upload multipart form containing a new survey to web service.
   * @param file
   * @param offices
   * @return
   * @throws IOException
   * @see http://hc.apache.org/httpcomponents-client-4.3.x/httpmime/examples/org/apache/http/examples/entity/mime/ClientMultipartFormPost.java
   */
  public static FormUploadResult uploadFile(OdkClient odkClient, RestTemplate restTemplate,
      MultipartFile file, List<String> offices) throws IOException {

    HttpComponentsClientHttpRequestFactoryDigestAuth requestFactory =
        (HttpComponentsClientHttpRequestFactoryDigestAuth) restTemplate.getRequestFactory();

    CloseableHttpClient httpClient = (CloseableHttpClient) requestFactory.getHttpClient();

    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

    String postUploadUrl = odkClient.getUrl(FORM_UPLOAD_ENDPOINT);

    int sizeOf = 0;

    for (String office : offices) {
      logger.info("Office bytes: " + office.getBytes().length);
      sizeOf += office.getBytes().length;
      builder.addTextBody(GeneralConsts.OFFICE_ID, office, ContentType.TEXT_PLAIN);
    }

    builder.addBinaryBody(GeneralConsts.ZIP_FILE, file.getBytes(),
        ContentType.create("application/zip"), "temp.zip");
    logger.info("File bytes: " + file.getBytes().length);
    sizeOf += file.getBytes().length;
    logger.info("Size of all content: " + sizeOf);

    HttpEntity requestEntity = builder.build();

    HttpPost httpPost = new HttpPost(postUploadUrl);
    httpPost.setEntity(requestEntity);
    CloseableHttpResponse response = httpClient.execute(httpPost);
    HttpEntity responseEntity = response.getEntity();
    FormUploadResult formUploadResult = mapper.readValue(responseEntity.getContent(), FormUploadResult.class);

    EntityUtils.consume(responseEntity);

    return formUploadResult;
  }

}
