package org.benetech.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.ajax.AjaxFormResponse;
import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.model.form.ChangePasswordAdminForm;
import org.benetech.model.form.UserEntityForm;
import org.benetech.util.OdkClientUtils;
import org.benetech.validator.ChangePasswordAdminFormValidator;
import org.benetech.validator.UserEntityFormValidator;
import org.opendatakit.api.offices.entity.RegionalOffice;
import org.opendatakit.api.users.entity.RoleDescription;
import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserAdminController {

  private static Log logger = LogFactory.getLog(UserAdminController.class);


  @Autowired
  OdkClientFactory odkClientFactory;

  @Autowired
  UserEntityFormValidator userEntityFormValidator;

  @InitBinder("user")
  protected void initUserBinder(WebDataBinder binder) {
    binder.setValidator(userEntityFormValidator);
  }



  private void populateDefaultModel(Model model) {
    OdkClient odkClient = odkClientFactory.getOdkClient();
    List<RoleDescription> roles = odkClient.getRoleList();
    Map<String, RegionalOffice> offices = OdkClientUtils.getOfficeMap(odkClient.getOfficeList());
    List<UserEntityForm> users = odkClient.getUserAuthorityGrid();
    model.addAttribute("offices", offices);
    model.addAttribute("roleDescriptions", roles);
    model.addAttribute("users", users);
  }

  @Secured({"ROLE_SITE_ACCESS_ADMIN"})
  @GetMapping("/admin/users")
  public String userGrid(Model model) {
    populateDefaultModel(model);
    model.addAttribute("passwordForm", new ChangePasswordAdminForm());

    return "admin_user_grid";
  }



  @Secured({"ROLE_SITE_ACCESS_ADMIN"})
  @PostMapping("/admin/users/delete")
  public String deleteUser(@ModelAttribute("user") UserEntityForm user, Model model) {
    OdkClient odkClient = odkClientFactory.getOdkClient();
    logger.info("Updating user " + user);

    HttpStatus status = odkClient.deleteUser(user.getUsername());
    logger.info("Result HTTP status: " + status.name());
    populateDefaultModel(model);
    model.addAttribute("msg", "User " + user.getUsername() + " has been deleted.");
    model.addAttribute("css", "danger");
    return "admin_user_grid";
  }

}
