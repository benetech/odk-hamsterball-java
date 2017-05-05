package org.benetech.client;

import java.net.URL;

import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class OdkClient {
  
  public static String USER_LIST_ENDPOINT = "/users/list";
  public static String USER_CURRENT_ENDPOINT = "/users/current";

  public static String ROLES_GRANTED_ENDPOINT = "/roles/granted";

  RestTemplate restTemplate;
  URL odkUrl;
  
  public OdkClient(RestTemplate restTemplate, URL odkUrl) {
    this.restTemplate = restTemplate;
    this.odkUrl = odkUrl;
  }
  
  public UserEntity getCurrentUser() {
    String getUserUrl = odkUrl.toExternalForm() + USER_CURRENT_ENDPOINT ;
    ResponseEntity<UserEntity> getResponse =
        restTemplate.exchange(getUserUrl, HttpMethod.GET, null, UserEntity.class);
    return getResponse.getBody();
  }

}
