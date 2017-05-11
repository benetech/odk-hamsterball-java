package org.benetech.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.util.HttpProxyUtil;
import org.opendatakit.api.forms.entity.FormUploadResult;
import org.opendatakit.api.offices.entity.RegionalOffice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
    HttpProxyUtil.proxyRequest(request, response, endpointUrl.toString());
  }
  

}
