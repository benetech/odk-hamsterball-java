package org.benetech.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.validator.OfficeFormValidator;
import org.opendatakit.api.offices.entity.RegionalOffice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OfficeAdminController {

  private static Log logger = LogFactory.getLog(OfficeAdminController.class);


  @Autowired
  OdkClientFactory odkClientFactory;

  @Autowired
  OfficeFormValidator officeFormValidator;

  @InitBinder("office")
  protected void initOfficeBinder(WebDataBinder binder) {
    binder.setValidator(officeFormValidator);
  }



  private void populateDefaultModel(Model model) {
    OdkClient odkClient = odkClientFactory.getOdkClient();

    List<RegionalOffice> offices = odkClient.getOfficeList();
    model.addAttribute("offices", offices);


  }

  @Secured({"ROLE_SITE_ACCESS_ADMIN"})
  @GetMapping("/admin/offices")
  public String officeGrid(Model model) {
    populateDefaultModel(model);
    return "office_grid";
  }



  @Secured({"ROLE_SITE_ACCESS_ADMIN"})
  @PostMapping("/admin/offices/delete")
  public String deleteOffice(@ModelAttribute("office") RegionalOffice office, Model model) {
    OdkClient odkClient = odkClientFactory.getOdkClient();
    logger.info("Updating office " + office.getOfficeId());

    HttpStatus status = odkClient.deleteOffice(office.getOfficeId());
    logger.info("Result HTTP status: " + status.name());
    populateDefaultModel(model);
    model.addAttribute("msg",
        "Office " + office.getName() + " (" + office.getOfficeId() + ") has been deleted.");
    model.addAttribute("css", "info");
    return "office_grid";
  }

}
