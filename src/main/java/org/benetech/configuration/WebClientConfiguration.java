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
  
  @Bean(name = "webServicesProperties")
  public Properties webServicesProperties() {
    Properties properties = new Properties();
    properties.setProperty("odk.url", odkUrl);
    properties.setProperty("odk.realm", odkRealm);
    return properties;
  }
  
  
  
}
