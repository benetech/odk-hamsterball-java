package org.benetech.controller;

import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.util.OdkClientUtils;
import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class WhoAmIController {
  
  @Autowired
  OdkClientFactory odkClientFactory;

  @RequestMapping("/whoami")
  public String whoami(Model model, Authentication authentication) {
    
      model.addAttribute("username", authentication.getName());
      model.addAttribute("roles", authentication.getAuthorities());
      OdkClient odkClient = odkClientFactory.getOdkClient();
      UserEntity user = odkClient.getCurrentUser();
      model.addAttribute("officeId", user.getOfficeId());
      model.addAttribute("fullName", user.getFullName());
      return "whoami";
  }
}
