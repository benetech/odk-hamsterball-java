package org.benetech.controller;

import java.util.List;
import java.util.Map;

import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.util.OdkClientUtils;
import org.opendatakit.api.offices.entity.RegionalOffice;
import org.opendatakit.api.users.entity.RoleDescription;
import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserAdminController {
  
  @Autowired
  OdkClientFactory odkClientFactory;

  @Secured({"ROLE_SITE_ACCESS_ADMIN"})
  @RequestMapping("/admin/users")
  public String usergrid(Model model) {
      OdkClient odkClient = odkClientFactory.getOdkClient();
      List<RoleDescription> roles = odkClient.getRoleList();
      Map<String, RegionalOffice> offices = OdkClientUtils.getOfficeMap(odkClient.getOfficeList());
      List<UserEntity> users = odkClient.getUserAuthorityGrid();
      model.addAttribute("offices", offices);
      model.addAttribute("roles", roles);
      model.addAttribute("users", users);
      return "admin_user_grid";
  }
}
