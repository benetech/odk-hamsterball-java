package org.benetech.security;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.benetech.client.OdkClient;
import org.benetech.constants.GeneralConsts;
import org.benetech.security.client.digest.DigestRestTemplateFactory;
import org.benetech.util.OdkClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.client.RestTemplate;

public class WebServiceDelegatingAuthenticationProvider implements AuthenticationProvider {

  
  Properties webServicesProperties;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String odkUrlString = webServicesProperties.getProperty("odk.url");
    String odkRealmName = webServicesProperties.getProperty("odk.realm");
    Map<String,Object> userDetails = new HashMap<String,Object>();
    
    URL odkUrl = null;
    URI odkUri = null;
    if (odkUrlString == null) {
      throw new InternalAuthenticationServiceException(
          "Host address is blank.  Did you configure the web service host?");
    }
    try {
      odkUrl = new URL(odkUrlString);
      odkUri = odkUrl.toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new InternalAuthenticationServiceException(
          "Bad host syntax.  Did you configure the web service host?");
    }

    String username = (String) authentication.getPrincipal();
    String password = authentication.getCredentials().toString();

    RestTemplate restTemplate = DigestRestTemplateFactory.getRestTemplate(odkUri.getHost(),
        odkUri.getPort(), odkUri.getScheme(), odkRealmName, username, password);
    String getRolesGrantedUrl = odkUrl.toExternalForm() + OdkClient.ROLES_GRANTED_ENDPOINT;
    ResponseEntity<List<String>> getResponse = restTemplate.exchange(getRolesGrantedUrl,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});

    userDetails.put(GeneralConsts.ODK_CLIENT, restTemplate);
    
    if (getResponse.getStatusCode().equals(HttpStatus.OK)) {
      Set<GrantedAuthority> authorized = new HashSet<GrantedAuthority>();
      for (String role : getResponse.getBody()) {
        authorized.add(new SimpleGrantedAuthority((String) role));
      }
      UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password, authorized);
      token.setDetails(userDetails);
      return token;
    } else {
      // Add more error cases here, or research how it is handled by default.
      // "Bad Credentials" is only one potential cause.
      throw new BadCredentialsException("Unable to log in to remote web service.");
    }

  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }

  public Properties getWebServicesProperties() {
    return webServicesProperties;
  }

  public void setWebServicesProperties(Properties webServicesProperties) {
    this.webServicesProperties = webServicesProperties;
  }

  
}
