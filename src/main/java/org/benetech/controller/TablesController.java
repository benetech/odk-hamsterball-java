package org.benetech.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.model.display.OdkTablesFileManifestEntryDisplay;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifest;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifestEntry;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.api.forms.entity.FormUploadResult;
import org.opendatakit.api.offices.entity.RegionalOffice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class TablesController {

  @Autowired
  OdkClientFactory odkClientFactory;

  private static Log logger = LogFactory.getLog(TablesController.class);

  @RequestMapping("/tables/manifest/{tableId}")
  public String tables(@PathVariable("tableId") String tableId, Model model) {

    OdkClient odkClient = odkClientFactory.getOdkClient();
    OdkTablesFileManifest manifest = odkClient.getTableManifest(tableId);
    model.addAttribute("manifest", manifest);
    model.addAttribute("tableId", tableId);
    return "manifest";
  }

  @RequestMapping("/tables/attachments/{tableId}")
  public String attachments(@PathVariable("tableId") String tableId, Model model) {

    OdkClient odkClient = odkClientFactory.getOdkClient();
    TableResource tableResource = odkClient.getTableResource(tableId);
    OdkTablesFileManifest manifest =
        odkClient.getTableAttachmentManifest(tableId, tableResource.getSchemaETag());
    List<OdkTablesFileManifestEntryDisplay> manifestDisplayList =
        new ArrayList<OdkTablesFileManifestEntryDisplay>();
    for (OdkTablesFileManifestEntry manifestEntry : manifest.getFiles()) {
      manifestDisplayList.add(new OdkTablesFileManifestEntryDisplay(manifestEntry));
    }
    model.addAttribute("manifest", manifestDisplayList);
    model.addAttribute("tableId", tableId);
    return "attachments";
  }

  @GetMapping("/tables/export/{tableId}")
  public String exportForm(@PathVariable("tableId") String tableId, Model model) {
    OdkClient odkClient = odkClientFactory.getOdkClient();
    model.addAttribute("tableId", tableId);
    return "export";
  }

  @RequestMapping("/tables/rows/{tableId}")
  public String rows(@PathVariable("tableId") String tableId,
      @RequestParam(name = "sortColumn", defaultValue = "savepointTimestamp",
          required = false) String sortColumn,
      @RequestParam(name = "ascending", defaultValue = "false", required = false) boolean ascending,
      Model model) {

    OdkClient odkClient = odkClientFactory.getOdkClient();
    TableResource tableResource = odkClient.getTableResource(tableId);
    RowResourceList rowResourceList =
        odkClient.getRowResourceList(tableId, tableResource.getSchemaETag(), sortColumn, ascending);
    model.addAttribute("tableResource", tableResource);
    model.addAttribute("rowResourceList", rowResourceList);
    model.addAttribute("tableId", tableId);
    model.addAttribute("ascending", ascending);
    logger.info("sortColumn: " + sortColumn + " ascending: " + ascending);
    model.addAttribute("sortColumn", sortColumn);

    return "rows";
  }

  @GetMapping("/tables/upload")
  public String uploadForm(Model model) {

    OdkClient odkClient = odkClientFactory.getOdkClient();
    List<RegionalOffice> offices = odkClient.getOfficeList();
    model.addAttribute("offices", offices);
    return "upload_form_template";
  }

  @PostMapping("/tables/upload")
  public String uploadSubmit(@RequestParam("zipFile") MultipartFile file,
      @RequestParam("officeId") List<String> offices, Model model) {

    OdkClient odkClient = odkClientFactory.getOdkClient();
    FormUploadResult result;
    try {
      result = odkClient.uploadFile(file, offices);
      model.addAttribute("result", result);
      model.addAttribute("msg", "File uploaded.");
      model.addAttribute("css", "success");
    } catch (IOException e) {
      model.addAttribute("msg", "Upload failed.");
      model.addAttribute("css", "danger");

    }
    List<RegionalOffice> menuOffices = odkClient.getOfficeList();

    model.addAttribute("offices", menuOffices);

    return "upload_form_template";
  }

}
