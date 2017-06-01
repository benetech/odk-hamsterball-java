package org.benetech.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.opendatakit.api.users.entity.RoleDescription;
import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WhoAmIController {

  Log logger = LogFactory.getLog(WhoAmIController.class);

  
  @Autowired
  OdkClientFactory odkClientFactory;

  @RequestMapping("/whoami")
  public String whoami(Model model, Authentication authentication, HttpSession session) {

    model.addAttribute("username", authentication.getName());
    model.addAttribute("roles", authentication.getAuthorities());
    OdkClient odkClient = odkClientFactory.getOdkClient();
    UserEntity user = odkClient.getCurrentUser();
    model.addAttribute("officeId", user.getOfficeId());
    model.addAttribute("fullName", user.getFullName());

    List<RoleDescription> roles = odkClient.getRoleList();
    Map<String, String> roleDescriptions = new HashMap<String, String>();
    for (RoleDescription role : roles) {
      roleDescriptions.put(role.getRole(), role.getName());
    }
    model.addAttribute("roleDescriptions", roleDescriptions);

    return "whoami";
  }
}
