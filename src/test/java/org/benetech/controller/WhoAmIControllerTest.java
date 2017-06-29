package org.benetech.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.configuration.MvcConfiguration;
import org.benetech.configuration.SecurityConfiguration;
import org.benetech.configuration.WebClientConfiguration;
import org.benetech.constants.GeneralConsts;
import org.benetech.interceptor.MenuInterceptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@ActiveProfiles({"unittest"})
@WebMvcTest({WhoAmIController.class, MenuInterceptor.class})
@ContextConfiguration(
    classes = {MvcConfiguration.class, WebClientConfiguration.class, SecurityConfiguration.class})
public class WhoAmIControllerTest {

  Log logger = LogFactory.getLog(WhoAmIControllerTest.class);

  private static final String BOGUS_ROOT_URL = "http://hamster.tech";
  
  private RestTemplate restTemplate;

  @Autowired
  Properties webServicesProperties;
  
  /**
   * To use the autowired default form of MockMvc, we cannot use interceptors. This means that we
   * cannot use the MenuInterceptor, and the "currentUser" variable is not set in the top menu.
   */
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private OdkClientFactory odkClientFactory;

  @Test
  public void whoAmIPage() throws Exception {

    logger.info("Starting whoAmIPage test");
    String odkRealm = webServicesProperties.getProperty("odk.realm");

    // Setup data + environment
    restTemplate = new RestTemplate();
    MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
    OdkClient mockOdkClient = new OdkClient(restTemplate, new URL(BOGUS_ROOT_URL), "default", "2", odkRealm);
    when(odkClientFactory.getOdkClient()).thenReturn(mockOdkClient);
    mockServer.expect(times(2), requestTo(BOGUS_ROOT_URL + "/users/current"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(
            "{ \"roles\" : [\"ROLE_USER\",\"ROLE_DATA_VIEWER\"],\"officeId\" : \"capiatá\",\"user_id\" : \"username:javier\", \"full_name\" : \"Javier Cáceres\"}",
            MediaType.APPLICATION_JSON));
    mockServer.expect(requestTo(BOGUS_ROOT_URL + "/roles/list")).andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(
            "[{ \"role\" : \"ROLE_USER\", \"name\" : \"User\", \"description\" : \"Basic logged in user\"},{ \"role\" : \"ROLE_DATA_VIEWER\", \"name\" : \"Data Viewer\", \"description\" : \"Required to view submissions\"}]",
            MediaType.APPLICATION_JSON));

    // Run the test
    // Check the results
    logger.info("Calling /whoami");
    this.mockMvc.perform(get("/whoami").session(getMockHttpSession())).andDo(print())
        .andExpect(status().isOk()).andExpect(content().string(containsString("Who Am I?")));
  }

  /**
   * Instead of mocking the login process, we inject a security context into the HTTP Session that
   * makes it look like the user has already logged in.
   * @return
   */
  private MockHttpSession getMockHttpSession() {
    MockHttpSession httpSession = new MockHttpSession();
    httpSession.setAttribute("SPRING_SECURITY_CONTEXT", getMockSecurityContext());
    return httpSession;
  }

  /** 
   * Populate a security context for a logged-in user.
   * @return
   */
  private SecurityContext getMockSecurityContext() {
    SecurityContextImpl securityContext = new SecurityContextImpl();
    Set<GrantedAuthority> authorized = new HashSet<GrantedAuthority>();
    authorized.add(new SimpleGrantedAuthority((String) "ROLE_USER"));
    authorized.add(new SimpleGrantedAuthority((String) "ROLE_DATA_VIEWER"));
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken("javier", "password", authorized);
    securityContext.setAuthentication(token);
    
    Map<String, Object> userDetails = new HashMap<String, Object>();
    userDetails.put(GeneralConsts.ODK_REST_CLIENT, restTemplate);
    // Cached credentials for file upload form / pre-emptive digest authentication
    UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials("javier",
        "password");
    userDetails.put(GeneralConsts.PREEMPTIVE_CREDENTIALS, usernamePasswordCredentials);
    token.setDetails(userDetails);

    return securityContext;
  }

}
