package org.benetech.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.benetech.security.client.digest.HttpComponentsClientHttpRequestFactoryDigestAuth;
import org.springframework.web.client.RestTemplate;

/**
 * Some requests we'd just like to proxy, like file requests.
 * 
 * @author Serge Ballesta
 * @author Mateusz Radziszewski
 * @see http://stackoverflow.com/questions/31452074/how-to-proxy-http-requests-in-spring-mvc
 */
public class HttpProxyUtils {

  private static Log logger = LogFactory.getLog(HttpProxyUtils.class);

  public static void proxyRequest(HttpServletRequest request, HttpServletResponse response,
      String endpointUrl) {
    try {
      HttpUriRequest proxiedRequest = createHttpUriRequest(request, endpointUrl);
      HttpResponse proxiedResponse = getHttpClient().execute(proxiedRequest);
      writeToResponse(proxiedResponse, response);
    } catch (URISyntaxException e) {
      logger.error(e);
    } catch (ClientProtocolException e) {
      logger.error(e);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  public static HttpResponse proxyInterceptRequest(HttpServletRequest request,
      HttpServletResponse response, String endpointUrl) {
    HttpResponse proxiedResponse = null;

    try {
      HttpUriRequest proxiedRequest = createHttpUriRequest(request, endpointUrl);
      proxiedResponse = getHttpClient().execute(proxiedRequest);
      return  proxiedResponse;
    } catch (URISyntaxException e) {
      logger.error(e);
    } catch (ClientProtocolException e) {
      logger.error(e);
    } catch (IOException e) {
      logger.error(e);
    }
    return proxiedResponse;
  }

  private static HttpClient getHttpClient() {
    RestTemplate restTemplate = OdkClientUtils.getRestTemplate();

    HttpComponentsClientHttpRequestFactoryDigestAuth requestFactory =
        (HttpComponentsClientHttpRequestFactoryDigestAuth) restTemplate.getRequestFactory();

    return requestFactory.getHttpClient();
  }

  private static void writeToResponse(HttpResponse proxiedResponse, HttpServletResponse response) {
    for (Header header : proxiedResponse.getAllHeaders()) {
      if ((!header.getName().equals("Transfer-Encoding"))
          || (!header.getValue().equals("chunked"))) {

        response.addHeader(header.getName(), header.getValue());
      }
    }
    OutputStream os = null;
    InputStream is = null;
    try {
      is = proxiedResponse.getEntity().getContent();
      os = response.getOutputStream();
      IOUtils.copy(is, os);
    } catch (IOException e) {
      logger.error(e);
    } finally {
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          logger.error(e);
        }
      }
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          logger.error(e);

        }
      }
    }
  }

  private static HttpUriRequest createHttpUriRequest(HttpServletRequest request, String endpointUrl)
      throws URISyntaxException, MalformedURLException {
    URL targetUrl = null;
    URI targetUri = null;
    targetUrl = new URL(endpointUrl);
    targetUri = targetUrl.toURI();
    URI uri = new URI(targetUri.getScheme(), targetUri.getAuthority(), targetUri.getPath(),
        request.getQueryString(), // ignore the query part of the input url
        targetUri.getFragment());

    logger.info("Making request to  " + uri.toString());
    RequestBuilder rb = RequestBuilder.create(request.getMethod());
    rb.setUri(uri);

    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      String headerValue = request.getHeader(headerName);
      rb.addHeader(headerName, headerValue);
      logger.info("header: " + headerName + ": " + headerValue);
      
    }

    HttpUriRequest proxiedRequest = rb.build();
    return proxiedRequest;
  }
}
