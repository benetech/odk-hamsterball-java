package org.benetech.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.client.OdkClientFactory;
import org.benetech.util.HttpProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ProxyController {

  @Autowired
  OdkClientFactory odkClientFactory;

  private static Log logger = LogFactory.getLog(ProxyController.class);

  /**
   * Sometimes we just want to pass through a request to the web service.
   */
  @RequestMapping("/file/**")
  public void proxyFileRequests(HttpServletRequest request, HttpServletResponse response) {
    StringBuffer endpointUrl =
        new StringBuffer(odkClientFactory.getOdkClient().getFileProxyEndpoint());
    logger.info("endpointUrl: " + endpointUrl);
    String requestUrl = request.getRequestURI().substring(request.getContextPath().length() + "/file".length());
    logger.info("requestUrl: " + requestUrl);
    endpointUrl.append(requestUrl);
    HttpProxyUtils.proxyRequest(request, response, endpointUrl.toString());
  }
  
  /**
   * Sometimes we just want to pass through a request to the web service.
   */
  @RequestMapping("/tables/{tableId}/export/**")
  public void proxyExportRequests(HttpServletRequest request, HttpServletResponse response) {
    
    // Target endpoint: /odktables/{appId}/tables/{tableId}/export/{format}/showDeleted/{showDeleted}
    StringBuffer endpointUrl =
        new StringBuffer(odkClientFactory.getOdkClient().getTableExportProxyEndpoint());
    logger.info("endpointUrl: " + endpointUrl);
    String requestUrl = request.getRequestURI().substring(request.getContextPath().length());
    logger.info("requestUrl: " + requestUrl);
    endpointUrl.append(requestUrl);
    HttpProxyUtils.proxyRequest(request, response, endpointUrl.toString());
  }

}
