package org.benetech.client;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.constants.GeneralConsts;
import org.benetech.model.form.UserEntityForm;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifest;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.api.forms.entity.FormUploadResult;
import org.opendatakit.api.offices.entity.RegionalOffice;
import org.opendatakit.api.users.entity.RoleDescription;
import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

public class OdkClient {

  private static Log logger = LogFactory.getLog(OdkClient.class);

  public static String USER_LIST_ENDPOINT = "/users/list";
  public static String USER_CURRENT_ENDPOINT = "/users/current";
  public static String USER_CHANGE_PASSWORD = "/users/current/password";
  public static String OFFICES_ENDPOINT = "/offices";
  public static String FORM_UPLOAD_ENDPOINT = "/forms/{appId}/{odkClientVersion}";

  public static String ROLES_GRANTED_ENDPOINT = "/roles/granted";
  public static String ROLES_LIST_ENDPOINT = "/roles/list";

  public static String ADMIN_USERS_ENDPOINT = "/admin/users";
  public static String ADMIN_DELETE_USER_ENDPOINT = "/admin/users/username:{username}";
  public static String ADMIN_CHANGE_PASSWORD = "/admin/users/username:{username}/password";

  public static String TABLES_ENDPOINT = "/odktables/{appId}/tables";
  public static String TABLE_MANIFEST_ENDPOINT =
      "/odktables/{appId}/manifest/{odkClientVersion}/{tableId}";
  public static String TABLE_ROWS_ENDPOINT =
      "/odktables/{appId}/tables/{tableId}/ref/{schemaETag}/rows";
  public static String TABLE_ATTACHMENT_MANIFEST_ENDPOINT =
      "/odktables/{appId}/tables/{tableId}/ref/{schemaETag}/attachments/manifest";

  public static String TABLE_FILE_PROXY_ENDPOINT = "/odktables/{appId}/files/{odkClientVersion}";
  public static String TABLE_EXPORT_PROXY_ENDPOINT = "/odktables/{appId}";
  public static String TABLE_ATTACHMENT_PROXY_ENDPOINT = "";



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

  public String getFileProxyEndpoint() {
    return getUrl(TABLE_FILE_PROXY_ENDPOINT);
  }
  
  public String getTableExportProxyEndpoint() {
    return getUrl(TABLE_EXPORT_PROXY_ENDPOINT);
  }
  
  public String getAttachmentProxyEndpoint() {
    return getUrl(TABLE_ATTACHMENT_PROXY_ENDPOINT);
  }

  public String getFormUploadEndpoint() {
    return getUrl(FORM_UPLOAD_ENDPOINT);
  }

  public UserEntity getCurrentUser() {
    String getUserUrl = odkUrl.toExternalForm() + USER_CURRENT_ENDPOINT;
    ResponseEntity<UserEntity> getResponse =
        restTemplate.exchange(getUserUrl, HttpMethod.GET, null, UserEntity.class);
    return getResponse.getBody();
  }

  
  public HttpStatus deleteUser(String username) {
    String deleteUserUrl = odkUrl.toExternalForm() + ADMIN_DELETE_USER_ENDPOINT.replace("{username}", username);
    ResponseEntity<UserEntity> getResponse =
        restTemplate.exchange(deleteUserUrl, HttpMethod.DELETE, null, UserEntity.class);
    return getResponse.getStatusCode();
  }
  
  public void setCurrentUserPassword(String password) {
    String changePasswordUrl = odkUrl.toExternalForm() + USER_CHANGE_PASSWORD;
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add("Content-Type", "application/json");
    HttpEntity<?> request = new HttpEntity<>(password, headers);
    ResponseEntity<String> postResponse =
        restTemplate.postForEntity(changePasswordUrl, request, String.class);
  }
  
  public HttpStatus updateUser(UserEntity userEntity) {
    String postUserUrl = odkUrl.toExternalForm() + ADMIN_USERS_ENDPOINT;

    HttpEntity<UserEntity> postUserEntity = new HttpEntity<>(userEntity);
    ResponseEntity<UserEntity> postResponse =
        restTemplate.exchange(postUserUrl, HttpMethod.POST, postUserEntity, UserEntity.class);
    
    return postResponse.getStatusCode();
  }

  public HttpStatus changePasswordUser(String username, String password) {
    String postUserUrl = odkUrl.toExternalForm() + ADMIN_CHANGE_PASSWORD.replace("{username}", username);

    MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add("Content-Type", "application/json");
    HttpEntity<?> request = new HttpEntity<>(password, headers);

    // Submit a new user
    ResponseEntity<String> postResponse =
        restTemplate.exchange(postUserUrl, HttpMethod.POST, request, String.class);
    
    return postResponse.getStatusCode();
  }
  
  public List<UserEntityForm> getUserAuthorityGrid() {
    String getUserListUrl = odkUrl.toExternalForm() + ADMIN_USERS_ENDPOINT;
    ResponseEntity<List<UserEntity>> getResponse = restTemplate.exchange(getUserListUrl,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<UserEntity>>() {});
    List<UserEntityForm> entityFormList = new ArrayList<UserEntityForm>();
    for (UserEntity userEntity: getResponse.getBody()) {
      entityFormList.add(new UserEntityForm(userEntity));
    }
    return entityFormList;
  }

  public List<RoleDescription> getRoleList() {
    String getUserListUrl = odkUrl.toExternalForm() + ROLES_LIST_ENDPOINT;
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
    String getTablesUrl = getUrl(TABLES_ENDPOINT);

    ResponseEntity<TableResourceList> getResponse = restTemplate.exchange(getTablesUrl,
        HttpMethod.GET, null, new ParameterizedTypeReference<TableResourceList>() {});
    TableResourceList tables = getResponse.getBody();

    List<String> tableIds = new ArrayList<String>();
    for (TableResource table : tables.getTables()) {
      tableIds.add(table.getTableId());
    }
    return tableIds;
  }

  public OdkTablesFileManifest getTableManifest(String tableId) {

    String getManifestUrl = getUrl(TABLE_MANIFEST_ENDPOINT).replace("{tableId}", tableId);
    logger.info("Calling " + getManifestUrl);

    ResponseEntity<OdkTablesFileManifest> getResponse = restTemplate.exchange(getManifestUrl,
        HttpMethod.GET, null, new ParameterizedTypeReference<OdkTablesFileManifest>() {});
    OdkTablesFileManifest manifest = getResponse.getBody();

    return manifest;

  }

  public OdkTablesFileManifest getTableAttachmentManifest(String tableId, String schemaETag) {

    String getManifestUrl = getUrl(TABLE_ATTACHMENT_MANIFEST_ENDPOINT).replace("{tableId}", tableId)
        .replace("{schemaETag}", schemaETag);
    logger.info("Calling " + getManifestUrl);

    ResponseEntity<OdkTablesFileManifest> getResponse = restTemplate.exchange(getManifestUrl,
        HttpMethod.GET, null, new ParameterizedTypeReference<OdkTablesFileManifest>() {});
    OdkTablesFileManifest manifest = getResponse.getBody();

    return manifest;

  }

  public TableResource getTableResource(String tableId) {

    String getTableUrl = getUrl(TABLES_ENDPOINT) + "/" + tableId;
    logger.info("Calling " + getTableUrl);

    ResponseEntity<TableResource> getResponse = restTemplate.exchange(getTableUrl, HttpMethod.GET,
        null, new ParameterizedTypeReference<TableResource>() {});
    TableResource tableResource = getResponse.getBody();
    return tableResource;

  }

  public RowResourceList getRowResourceList(String tableId, String schemaETag) {

    String getRowListUrl = getUrl(TABLE_ROWS_ENDPOINT).replace("{tableId}", tableId)
        .replace("{schemaETag}", schemaETag);
    logger.info("Calling " + getRowListUrl);
    ResponseEntity<RowResourceList> getResponse = restTemplate.exchange(getRowListUrl,
        HttpMethod.GET, null, new ParameterizedTypeReference<RowResourceList>() {});
    RowResourceList tableResource = getResponse.getBody();
    return tableResource;

  }

  public String getUrl(String endpoint) {
    return odkUrl.toExternalForm()
        + (endpoint.replace("{appId}", odkAppId).replace("{odkClientVersion}", odkClientVersion));
  }

  /**
   * Upload a file, which is sent to the ODK Server
   * 
   * @param file
   * @param offices
   * @return
   * @throws IOException 
   * File needs to be converted to FileSystemResource before transmission.
   * @see http://stackoverflow.com/questions/41632647/multipart-file-upload-with-spring-resttemplate-and-jackson
   */
  public FormUploadResult uploadFile(MultipartFile file, List<String> offices) throws IOException {
    String postUploadUrl = odkUrl.toExternalForm() + (FORM_UPLOAD_ENDPOINT
        .replace("{appId}", odkAppId).replace("{odkClientVersion}", odkClientVersion));

    File tempFile = null;
    try {
      String extension = "." + FilenameUtils.getExtension(file.getOriginalFilename());
      tempFile = File.createTempFile("temp", extension);
      file.transferTo(tempFile);
    } catch (IOException e) {
      e.printStackTrace();
    }

    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();

    parts.add(GeneralConsts.ZIP_FILE, new FileSystemResource(tempFile));
    for (String office : offices) {
      parts.add(GeneralConsts.OFFICE_ID, office);
    }

    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.MULTIPART_FORM_DATA);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, header);
    ResponseEntity<FormUploadResult> entity =
        restTemplate.postForEntity(postUploadUrl, requestEntity, FormUploadResult.class);
    return entity.getBody();
  }

}
