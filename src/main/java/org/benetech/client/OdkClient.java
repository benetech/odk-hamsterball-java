package org.benetech.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.api.offices.entity.RegionalOffice;
import org.opendatakit.api.users.entity.RoleDescription;
import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class OdkClient {

  private static Log logger = LogFactory.getLog(OdkClient.class);


  public static String USER_LIST_ENDPOINT = "/users/list";
  public static String USER_CURRENT_ENDPOINT = "/users/current";
  public static String OFFICES_ENDPOINT = "/offices";

  public static String ROLES_GRANTED_ENDPOINT = "/roles/granted";

  public static String ADMIN_USERS_ENDPOINT = "/admin/users";
  public static String ADMIN_ROLES_ENDPOINT = "/admin/roles";

  public static String TABLES_ENDPOINT = "/odktables/{appId}/tables";


  private RestTemplate restTemplate;
  private URL odkUrl;
  private String odkAppId;
  private String odkClientVersion;

  public OdkClient(RestTemplate restTemplate, URL odkUrl, String odkAppId,
      String odkClientVersion) {
    this.restTemplate = restTemplate;
    this.odkUrl = odkUrl;
    this.odkAppId = odkAppId;
    this.odkClientVersion = odkClientVersion;
  }

  public UserEntity getCurrentUser() {
    String getUserUrl = odkUrl.toExternalForm() + USER_CURRENT_ENDPOINT;
    ResponseEntity<UserEntity> getResponse =
        restTemplate.exchange(getUserUrl, HttpMethod.GET, null, UserEntity.class);
    return getResponse.getBody();
  }

  public List<UserEntity> getUserAuthorityGrid() {
    String getUserListUrl = odkUrl.toExternalForm() + ADMIN_USERS_ENDPOINT;
    ResponseEntity<List<UserEntity>> getResponse = restTemplate.exchange(getUserListUrl,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<UserEntity>>() {});
    return getResponse.getBody();
  }

  public List<RoleDescription> getRoleList() {
    String getUserListUrl = odkUrl.toExternalForm() + ADMIN_ROLES_ENDPOINT;
    ResponseEntity<List<RoleDescription>> getResponse = restTemplate.exchange(getUserListUrl,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<RoleDescription>>() {});
    return getResponse.getBody();
  }

  public List<RegionalOffice> getOfficeList() {
    String getOfficeUrl = odkUrl.toExternalForm() + OFFICES_ENDPOINT;
    ResponseEntity<List<RegionalOffice>> getResponse = restTemplate.exchange(getOfficeUrl,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<RegionalOffice>>() {});
    return getResponse.getBody();
  }

  public List<String> getTableIds() {
    String getTablesUrl = odkUrl.toExternalForm() + (TABLES_ENDPOINT.replace("{appId}", odkAppId));

    ResponseEntity<TableResourceList> getResponse = restTemplate.exchange(getTablesUrl,
        HttpMethod.GET, null, new ParameterizedTypeReference<TableResourceList>() {});
    TableResourceList tables = getResponse.getBody();
    
    List<String> tableIds = new ArrayList<String>();
    for (TableResource table: tables.getTables()) {
      tableIds.add(table.getTableId());
    }
    
    return tableIds;
    
  }

}
