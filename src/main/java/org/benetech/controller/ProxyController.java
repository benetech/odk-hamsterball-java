package org.benetech.controller;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.client.OdkClientFactory;
import org.benetech.thumbnail.AttachmentThumbnailRepositoryImpl;
import org.benetech.thumbnail.Thumbnail;
import org.benetech.thumbnail.ThumbnailRepository;
import org.benetech.util.HttpProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ProxyController {

  @Autowired
  OdkClientFactory odkClientFactory;

  @Autowired
  ThumbnailRepository attachmentThumbnailRepository;
  
  private static Log logger = LogFactory.getLog(ProxyController.class);

  /**
   * Sometimes we just want to pass through a request to the web service.
   */
  @RequestMapping("/file/**")
  public void proxyFileRequests(HttpServletRequest request, HttpServletResponse response) {
    StringBuffer endpointUrl =
        new StringBuffer(odkClientFactory.getOdkClient().getFileProxyEndpoint());
    logger.debug("endpointUrl: " + endpointUrl);
    String requestUrl = request.getRequestURI().substring(request.getContextPath().length() + "/file".length());
    logger.info("requestUrl: " + requestUrl);
    endpointUrl.append(requestUrl);
    HttpProxyUtils.proxyRequest(request, response, endpointUrl.toString());
  }
  
  @RequestMapping("/attachment/**")
  public void proxyAttachmentRequests(HttpServletRequest request, HttpServletResponse response) {
    StringBuffer endpointUrl =
        new StringBuffer(odkClientFactory.getOdkClient().getAttachmentProxyEndpoint());
    logger.debug("endpointUrl: " + endpointUrl);
    String requestUrl = request.getRequestURI().substring(request.getContextPath().length() + "/attachment".length());
    logger.info("requestUrl: " + requestUrl);
    endpointUrl.append(requestUrl);
    HttpProxyUtils.proxyRequest(request, response, endpointUrl.toString());
  }
  
  @RequestMapping("/attachment/thumb/**")
  public void proxyAttachmentThumbnailRequests(HttpServletRequest request, HttpServletResponse response) {
    StringBuffer endpointUrl =
        new StringBuffer(odkClientFactory.getOdkClient().getAttachmentProxyEndpoint());
    logger.debug("endpointUrl: " + endpointUrl);
    String requestUrl = request.getRequestURI().substring(request.getContextPath().length() + "/attachment/thumb".length());
    logger.info("requestUrl: " + requestUrl);
    endpointUrl.append(requestUrl);
  
    Thumbnail thumbnail = attachmentThumbnailRepository.get(requestUrl, request, endpointUrl.toString());
    HttpProxyUtils.writeToResponse(thumbnail, response);
    
  }
  
  

  @RequestMapping("/tables/{tableId}/export/**")
  public void proxyExportRequests(HttpServletRequest request, HttpServletResponse response) {
    
    // Target endpoint: /odktables/{appId}/tables/{tableId}/export/{format}/showDeleted/{showDeleted}
    StringBuffer endpointUrl =
        new StringBuffer(odkClientFactory.getOdkClient().getTableExportProxyEndpoint());
    logger.debug("endpointUrl: " + endpointUrl);
    String requestUrl = request.getRequestURI().substring(request.getContextPath().length());
    logger.info("requestUrl: " + requestUrl);
    endpointUrl.append(requestUrl);
    HttpProxyUtils.proxyRequest(request, response, endpointUrl.toString());
  }
  
  

}
