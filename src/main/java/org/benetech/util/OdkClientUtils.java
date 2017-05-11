package org.benetech.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.client.OdkClient;
import org.benetech.constants.GeneralConsts;
import org.opendatakit.api.offices.entity.RegionalOffice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

public class OdkClientUtils {

  private static Log logger = LogFactory.getLog(OdkClientUtils.class);

  public static RestTemplate getRestTemplate(Authentication authentication) {
    RestTemplate restTemplate = null;
    try {
      Map<String, Object> userDetails = (Map<String, Object>) authentication.getDetails();

      if (userDetails == null || userDetails.get(GeneralConsts.ODK_CLIENT) == null) {
        // TODO: Get reauthenticated. Easy workaround may be to force logout for now.
      } else {
        restTemplate = (RestTemplate) userDetails.get(GeneralConsts.ODK_CLIENT);
      }
    } catch (ClassCastException e) {
      logger.info("Unable to get REST template for this user.");
    }
    return restTemplate;
  }

  public static RestTemplate getRestTemplate() {
    return getRestTemplate(SecurityContextHolder.getContext().getAuthentication());
  }

  public static Map<String, RegionalOffice> getOfficeMap(List<RegionalOffice> offices) {
    Map<String, RegionalOffice> result = new HashMap<String, RegionalOffice>();
    for (RegionalOffice office : offices) {
      result.put(office.getOfficeId(), office);
    }
    return result;
  }

}
