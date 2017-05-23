package org.benetech.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.benetech.constants.MimeTypes;
import org.benetech.security.client.digest.HttpComponentsClientHttpRequestFactoryDigestAuth;
import org.benetech.thumbnail.FileWrapper;
import org.benetech.thumbnail.Thumbnail;
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

	public static void proxyRequest(HttpServletRequest request, HttpServletResponse response, String endpointUrl) {
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

	public static HttpResponse proxyInterceptRequest(HttpServletRequest request, HttpServletResponse response,
			String endpointUrl) {
		HttpResponse proxiedResponse = null;

		try {
			HttpUriRequest proxiedRequest = createHttpUriRequest(request, endpointUrl);
			proxiedResponse = getHttpClient().execute(proxiedRequest);
			return proxiedResponse;
		} catch (URISyntaxException e) {
			logger.error(e);
		} catch (ClientProtocolException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return proxiedResponse;
	}

	/**
	 * Forward a regular get request to a web service that returns a file, then
	 * intercept that file.
	 * 
	 * @param request
	 * @param response
	 * @param endpointUrl
	 * @return
	 */
	public static FileWrapper proxyInterceptFileRequest(HttpServletRequest request, String endpointUrl) {
		HttpResponse proxiedResponse = null;
		File fileResponse = null;
		FileWrapper fileWrapper = new FileWrapper();
		try {
			HttpUriRequest proxiedRequest = createHttpUriRequest(request, endpointUrl);
			proxiedResponse = getHttpClient().execute(proxiedRequest);
			HttpEntity entity = proxiedResponse.getEntity();
			String contentType = entity.getContentType().getValue();

			String suffix = MimeTypes.EXTENSIONS.get(contentType);
			fileWrapper.setExtension(suffix);
			fileWrapper.setContentType(contentType);
			fileResponse = File.createTempFile("temp", suffix);
			BufferedInputStream inputStream = new BufferedInputStream(entity.getContent());
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileResponse));
			int inByte;
			while ((inByte = inputStream.read()) != -1)
				outputStream.write(inByte);
			inputStream.close();
			outputStream.close();

		} catch (URISyntaxException e) {
			logger.error(e);
		} catch (ClientProtocolException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		fileWrapper.setFile(fileResponse);
		return fileWrapper;
	}

	private static HttpClient getHttpClient() {
		RestTemplate restTemplate = OdkClientUtils.getRestTemplate();

		HttpComponentsClientHttpRequestFactoryDigestAuth requestFactory = (HttpComponentsClientHttpRequestFactoryDigestAuth) restTemplate
				.getRequestFactory();

		return requestFactory.getHttpClient();
	}

	private static void writeToResponse(HttpResponse proxiedResponse, HttpServletResponse response) {
		for (Header header : proxiedResponse.getAllHeaders()) {
			if ((!header.getName().equals("Transfer-Encoding")) || (!header.getValue().equals("chunked"))) {

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

	public static void writeToResponse(Thumbnail thumbnail, HttpServletResponse response) {
		response.setContentType(thumbnail.getContentType());
		OutputStream os = null;
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(thumbnail.getThumbnail());
			logger.debug("Byte array length (thumbnail file size): " + thumbnail.getThumbnail().length);
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
				request.getQueryString(), // ignore the query part of the input
											// url
				targetUri.getFragment());

		logger.info("Making request to  " + uri.toString());
		RequestBuilder rb = RequestBuilder.create(request.getMethod());
		rb.setUri(uri);

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			String headerValue = request.getHeader(headerName);
			if ("host".equalsIgnoreCase(headerName)) {
				headerValue = targetUri.getHost();
			}
			rb.addHeader(headerName, headerValue);
		}

		HttpUriRequest proxiedRequest = rb.build();
		return proxiedRequest;
	}
}
