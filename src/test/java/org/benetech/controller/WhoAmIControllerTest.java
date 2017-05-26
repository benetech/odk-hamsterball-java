package org.benetech.controller;
import static org.springframework.http.HttpMethod.*; 
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*; 
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.configuration.MvcConfiguration;
import org.benetech.configuration.SecurityConfiguration;
import org.benetech.configuration.TestWebServiceConfiguration;
import org.benetech.configuration.WebClientConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.api.users.entity.RoleDescription;
import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@ActiveProfiles({"unittest"})
@WebMvcTest(WhoAmIController.class)
@ContextConfiguration(
    classes = {MvcConfiguration.class, SecurityConfiguration.class, WebClientConfiguration.class})
public class WhoAmIControllerTest {

  Log logger = LogFactory.getLog(WhoAmIControllerTest.class);


  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private OdkClientFactory odkClientFactory;

  @Test
  @Ignore
  public void whoAmIPage() throws Exception {

    logger.info("Starting whoAmIPage test");
    // Setup data
    UserEntity testUser = new UserEntity("username:javier", "Javier Cáceres", "capiatá",
        "ROLE_DATA_VIEWER", "ROLE_USER");

    // Setup environment
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
    OdkClient mockOdkClient = new OdkClient(restTemplate, new URL("http://bogus"), "default", "2");
    when(odkClientFactory.getOdkClient()).thenReturn(mockOdkClient);
    mockServer.expect(requestTo("/users/current")).andExpect(method(HttpMethod.GET))
    .andRespond(withSuccess("{ \"roles\" : [\"ROLE_USER\",\"ROLE_DATA_VIEWER\"],\"officeId\" : \"capiatá\",\"user_id\" : \"username:javier\", \"full_name\" : \"Javier Cáceres\"}", MediaType.APPLICATION_JSON));
    mockServer.expect(requestTo("/roles/list")).andExpect(method(HttpMethod.GET))
    .andRespond(withSuccess("[{ \"role\" : \"ROLE_USER\", \"name\" : \"User\", \"description\" : \"Basic logged in user\"},{ \"role\" : \"ROLE_DATA_VIEWER\", \"name\" : \"Data Viewer\", \"description\" : \"Required to view submissions\"}]", MediaType.APPLICATION_JSON));


    // Run the test
    // Check the results
    logger.info("Calling /whoami");
    this.mockMvc.perform(get("/whoami")).andDo(print()).andExpect(status().isOk())
        .andExpect(content().string(containsString("Who Am I?")));
  }

  private List<RoleDescription> getRoleDescriptions() {
    List<RoleDescription> roles = new ArrayList<RoleDescription>();
    RoleDescription roleDescription = new RoleDescription();
    roleDescription.setDescription("General user");
    roleDescription.setRole("ROLE_USER");
    roleDescription.setName("User");
    roles.add(roleDescription);
    roleDescription = new RoleDescription();
    roleDescription.setDescription("Person who can view data");
    roleDescription.setRole("ROLE_DATA_VIEWER");
    roleDescription.setName("Data Viewer");
    roles.add(roleDescription);

    return roles;
  }


}
