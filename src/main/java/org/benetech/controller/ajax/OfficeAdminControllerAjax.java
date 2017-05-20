package org.benetech.controller.ajax;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.ajax.AjaxFormResponse;
import org.benetech.ajax.AjaxFormResponseFactory;
import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.validator.OfficeFormValidator;
import org.opendatakit.api.offices.entity.RegionalOffice;
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
public class OfficeAdminControllerAjax {

  private static Log logger = LogFactory.getLog(OfficeAdminControllerAjax.class);

  @Autowired
  OdkClientFactory odkClientFactory;

  @Autowired
  OfficeFormValidator officeFormValidator;

  @Autowired
  AjaxFormResponseFactory responseFactory;

  @InitBinder("office")
  protected void initOfficeBinder(WebDataBinder binder) {
    binder.setValidator(officeFormValidator);
  }

  @Secured({"ROLE_SITE_ACCESS_ADMIN"})
  @PostMapping(value = "/admin/offices", produces = "application/json")
  public ResponseEntity<?> addUpdateOffice(@ModelAttribute("office") @Validated RegionalOffice office,
      BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      AjaxFormResponse response =
          responseFactory.getAjaxFormResponse(bindingResult, "Please correct errors in the form.");

      return ResponseEntity.badRequest().body(response);
    }
    OdkClient odkClient = odkClientFactory.getOdkClient();
    logger.info("Updating office " + office.getOfficeId());
    HttpStatus status = odkClient.updateOffice(office);
    AjaxFormResponse response = new AjaxFormResponse(
        "Office " + office.getName() + " (" + office.getOfficeId() + ") updated.");
    return ResponseEntity.ok(response);
  }
}
