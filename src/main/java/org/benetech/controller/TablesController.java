package org.benetech.controller;

import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TablesController {
  
  @Autowired
  OdkClientFactory odkClientFactory;

  @RequestMapping("/tables")
  public String tables(Model model) {
    

      OdkClient odkClient = odkClientFactory.getOdkClient();
      UserEntity user = odkClient.getCurrentUser();
      model.addAttribute("officeId", user.getOfficeId());
      model.addAttribute("fullName", user.getFullName());
      return "whoami";
  }
}
