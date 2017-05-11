package org.benetech.configuration;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
@ComponentScan(basePackages = {"org.benetech"})
public class WebClientConfiguration {

  @Value("${odk.url}")
  String odkUrl;
  
  @Value("${odk.realm}")
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

    return properties;
  }
  
  
  
}
