package org.benetech.controller.ajax;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.ajax.AjaxFormResponse;
import org.benetech.ajax.AjaxFormResponseFactory;
import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.model.form.ChangePasswordAdminForm;
import org.benetech.model.form.UserEntityForm;
import org.benetech.validator.ChangePasswordAdminFormValidator;
import org.benetech.validator.UserEntityFormValidator;
import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAdminControllerAjax {

  private static Log logger = LogFactory.getLog(UserAdminControllerAjax.class);


  @Autowired
  OdkClientFactory odkClientFactory;

  @Autowired
  UserEntityFormValidator userEntityFormValidator;

  @Autowired
  ChangePasswordAdminFormValidator ChangePasswordAdminFormValidator;
  
  @Autowired
  AjaxFormResponseFactory responseFactory;

  @InitBinder("user")
  protected void initUserBinder(WebDataBinder binder) {
    binder.setValidator(userEntityFormValidator);
  }

  @InitBinder("passwordForm")
  protected void initPasswordBinder(WebDataBinder binder) {
    binder.setValidator(ChangePasswordAdminFormValidator);
  }

  @Secured({"ROLE_SITE_ACCESS_ADMIN"})
  @PostMapping(value="/admin/users/password", produces = "application/json")
  public ResponseEntity<?> changePasswordUserAjax(
      @ModelAttribute("passwordForm") @Validated ChangePasswordAdminForm passwordForm,
      BindingResult bindingResult, Model model) {

    if (bindingResult.hasErrors()) {
      AjaxFormResponse response =
          responseFactory.getAjaxFormResponse(bindingResult, "Please correct errors in the form.");

      return ResponseEntity.badRequest().body(response);
    }

    OdkClient odkClient = odkClientFactory.getOdkClient();
    logger.debug("Updating user " + passwordForm.getUsername());

    HttpStatus status =
        odkClient.changePasswordUser(passwordForm.getUsername(), passwordForm.getPassword1());
    logger.debug("Result HTTP status: " + status.name());
    AjaxFormResponse response = new AjaxFormResponse("Password updated for user " + passwordForm.getUsername() + ".");
    return ResponseEntity.ok(response);
  }
  
  @Secured({"ROLE_SITE_ACCESS_ADMIN"})
  @PostMapping(value="/admin/users", produces = "application/json")
  public ResponseEntity<?> addUpdateUser(@ModelAttribute("user") @Validated UserEntityForm user,
      BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      AjaxFormResponse response =
          responseFactory.getAjaxFormResponse(bindingResult, "Please correct errors in the form.");

      return ResponseEntity.badRequest().body(response);
    }
    OdkClient odkClient = odkClientFactory.getOdkClient();
    logger.debug("Updating user " + user);
    UserEntity userEntity = (UserEntity) user;
    HttpStatus status = odkClient.updateUser(userEntity);
    AjaxFormResponse response = new AjaxFormResponse("User " + user.getUsername() + " updated.");
    return ResponseEntity.ok(response);
  }
}
