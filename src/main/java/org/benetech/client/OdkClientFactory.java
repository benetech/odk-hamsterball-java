package org.benetech.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.benetech.util.OdkClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.stereotype.Component;

@Component
public class OdkClientFactory {
  
  @Autowired
  Properties webServicesProperties; 

  public OdkClient getOdkClient() {
    String odkUrlString = webServicesProperties.getProperty("odk.url");
    
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
    
    return new OdkClient(OdkClientUtils.getRestTemplate(), odkUrl );
  }

  

}
