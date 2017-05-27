package org.benetech.configuration;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.controller.OfficeAdminController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"default","unittest","integrationtest"})
@ComponentScan(basePackages = {"org.benetech"})
public class WebClientConfiguration {

  private static Log logger = LogFactory.getLog(WebClientConfiguration.class);

  @Value("${odk.url:http://localhost:8080}")
  String odkUrl;
  
  @Value("${odk.realm:opendatakit.org ODK 2.0 Server}")
  String odkRealm;
  
  @Value("${odk.app.id:'default'}")
  String odkAppId;
  
  @Value("${odk.client.version:2}")
  String odkClientVersion;
  
  @Bean(name = "webServicesProperties")
  public Properties webServicesProperties() {
    Properties properties = new Properties();
    properties.setProperty("odk.url", odkUrl);
    properties.setProperty("odk.realm", odkRealm);
    properties.setProperty("odk.app.id", odkAppId);
    properties.setProperty("odk.client.version", odkClientVersion);
    logger.info("Web Service Properties");
    logger.info("odk.url: " + odkUrl );
    logger.info("odk.realm: " + odkRealm );
    logger.info("odk.app.id: " + odkAppId );
    logger.info("odk.client.version: " + odkClientVersion );

    return properties;
  }
  
  
  
}
