package org.benetech.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.util.OdkClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.stereotype.Component;

@Component
public class OdkClientFactory {

	private static Log logger = LogFactory.getLog(OdkClientFactory.class);

  @Autowired
  Properties webServicesProperties;

  public OdkClient getOdkClient() {
    String odkUrlString = webServicesProperties.getProperty("odk.url");
    String odkAppId = webServicesProperties.getProperty("odk.app.id");
    String odkClientVersion = webServicesProperties.getProperty("odk.client.version");
    
    logger.info("Setting odk.url to " + odkUrlString);
    logger.info("Setting odk.app.id to " + odkAppId);
    logger.info("Setting odk.client.version to " + odkClientVersion);


    URL odkUrl = null;
    if (odkUrlString == null) {
      throw new InternalAuthenticationServiceException(
          "Host address is blank.  Did you configure the web service host?");
    }
    try {
      odkUrl = new URL(odkUrlString);
    } catch (MalformedURLException e) {
      throw new InternalAuthenticationServiceException(
          "Bad host syntax.  Did you configure the web service host?");
    }

    return new OdkClient(OdkClientUtils.getRestTemplate(), odkUrl, odkAppId, odkClientVersion);
  }



}
