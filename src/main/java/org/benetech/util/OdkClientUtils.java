package org.benetech.util;

import java.util.Map;

import org.benetech.constants.GeneralConsts;
import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

public class OdkClientUtils {
  


  public static RestTemplate getRestTemplate(Authentication authentication){
    Map<String,Object> userDetails = (Map<String,Object>) authentication.getDetails();
    RestTemplate restTemplate = null;
    if (userDetails == null || userDetails.get(GeneralConsts.ODK_CLIENT) == null) {
      // TODO: Get reauthenticated.  Easy workaround may be to force logout for now.
    } else {
      restTemplate = (RestTemplate) userDetails.get(GeneralConsts.ODK_CLIENT);
    }
    return restTemplate;
  }
  
  public static RestTemplate getRestTemplate(){
    return getRestTemplate(SecurityContextHolder.getContext().getAuthentication());
  }
  
  
}
