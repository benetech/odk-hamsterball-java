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
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifest;
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

	public static String ADMIN_USERS_ENDPOINT = "/admin/users";
	public static String ADMIN_ROLES_ENDPOINT = "/admin/roles";
	public static String ADMIN_CHANGE_PASSWORD = "/admin/users/username:{username}/password";

	public static String TABLES_ENDPOINT = "/odktables/{appId}/tables";
	public static String TABLE_MANIFEST_ENDPOINT = "/odktables/{appId}/manifest/{odkClientVersion}/{tableId}";
	
	public static String TABLE_FILE_PROXY_ENDPOINT = "/odktables/{appId}/files/{odkClientVersion}";

	private RestTemplate restTemplate;
	private URL odkUrl;
	private String odkAppId;
	private String odkClientVersion;

	public OdkClient(RestTemplate restTemplate, URL odkUrl, String odkAppId, String odkClientVersion) {
		this.restTemplate = restTemplate;
		this.odkUrl = odkUrl;
		this.odkAppId = odkAppId;
		this.odkClientVersion = odkClientVersion;
	}

	public String getFileProxyEndpoint() {
	     return odkUrl.toExternalForm() + (TABLE_FILE_PROXY_ENDPOINT.replace("{appId}", odkAppId)
             .replace("{odkClientVersion}", odkClientVersion));
	}
	
	public String getFormUploadEndpoint() {
         return odkUrl.toExternalForm() + (FORM_UPLOAD_ENDPOINT.replace("{appId}", odkAppId)
             .replace("{odkClientVersion}", odkClientVersion));
    }
	
	public UserEntity getCurrentUser() {
		String getUserUrl = odkUrl.toExternalForm() + USER_CURRENT_ENDPOINT;
		ResponseEntity<UserEntity> getResponse = restTemplate.exchange(getUserUrl, HttpMethod.GET, null,
				UserEntity.class);
		return getResponse.getBody();
	}
	
	public void setCurrentUserPassword(String password) {
		String changePasswordUrl = odkUrl.toExternalForm() + USER_CHANGE_PASSWORD;
	    MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
	    headers.add("Content-Type", "application/json");
	    HttpEntity<?> request = new HttpEntity<>(password, headers);
	    ResponseEntity<String> postResponse =
	        restTemplate.postForEntity(changePasswordUrl, request, String.class);
	}

	public List<UserEntity> getUserAuthorityGrid() {
		String getUserListUrl = odkUrl.toExternalForm() + ADMIN_USERS_ENDPOINT;
		ResponseEntity<List<UserEntity>> getResponse = restTemplate.exchange(getUserListUrl, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<UserEntity>>() {
				});
		return getResponse.getBody();
	}

	public List<RoleDescription> getRoleList() {
		String getUserListUrl = odkUrl.toExternalForm() + ADMIN_ROLES_ENDPOINT;
		ResponseEntity<List<RoleDescription>> getResponse = restTemplate.exchange(getUserListUrl, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<RoleDescription>>() {
				});
		return getResponse.getBody();
	}

	public List<RegionalOffice> getOfficeList() {
		String getOfficeUrl = odkUrl.toExternalForm() + OFFICES_ENDPOINT;
		ResponseEntity<List<RegionalOffice>> getResponse = restTemplate.exchange(getOfficeUrl, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<RegionalOffice>>() {
				});
		return getResponse.getBody();
	}

	public List<String> getTableIds() {
		String getTablesUrl = odkUrl.toExternalForm() + (TABLES_ENDPOINT.replace("{appId}", odkAppId));

		ResponseEntity<TableResourceList> getResponse = restTemplate.exchange(getTablesUrl, HttpMethod.GET, null,
				new ParameterizedTypeReference<TableResourceList>() {
				});
		TableResourceList tables = getResponse.getBody();

		List<String> tableIds = new ArrayList<String>();
		for (TableResource table : tables.getTables()) {
			tableIds.add(table.getTableId());
		}
		return tableIds;
	}

	public OdkTablesFileManifest getTableManifest(String tableId) {

		String getManifestUrl = odkUrl.toExternalForm() + (TABLE_MANIFEST_ENDPOINT.replace("{appId}", odkAppId)
				.replace("{odkClientVersion}", odkClientVersion).replace("{tableId}", tableId));
		logger.info("Calling " + getManifestUrl);

		ResponseEntity<OdkTablesFileManifest> getResponse = restTemplate.exchange(getManifestUrl, HttpMethod.GET, null,
				new ParameterizedTypeReference<OdkTablesFileManifest>() {
				});
		OdkTablesFileManifest manifest = getResponse.getBody();

		return manifest;

	}
	
	/**
	 * Upload a file, which is sent to the ODK Server
	 * @param file
	 * @param offices
	 * @return
	 * @throws IOException
	 * File needs to be converted to FileSystemResource before transmission.
	 * @see http://stackoverflow.com/questions/41632647/multipart-file-upload-with-spring-resttemplate-and-jackson
	 */
	public FormUploadResult uploadFile(MultipartFile file, List<String> offices) throws IOException {
		String postUploadUrl = odkUrl.toExternalForm() + (FORM_UPLOAD_ENDPOINT.replace("{appId}", odkAppId)
				.replace("{odkClientVersion}", odkClientVersion));

		File tempFile = null;
	    try {
	        String extension = "." +  FilenameUtils.getExtension(file.getOriginalFilename());
	        tempFile = File.createTempFile("temp", extension);
	        file.transferTo(tempFile);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		
		parts.add(GeneralConsts.ZIP_FILE, new FileSystemResource(tempFile));
		for (String office: offices) {
			parts.add(GeneralConsts.OFFICE_ID, office);
		}
		
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.MULTIPART_FORM_DATA);
		
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, header);
		ResponseEntity<FormUploadResult> entity = restTemplate.postForEntity(postUploadUrl, requestEntity, FormUploadResult.class);
		return entity.getBody();
	}

}
