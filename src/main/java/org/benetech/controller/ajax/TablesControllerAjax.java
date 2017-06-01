package org.benetech.controller.ajax;

import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.ajax.AjaxFormResponse;
import org.benetech.ajax.AjaxFormResponseFactory;
import org.benetech.ajax.SurveyQuestion;
import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.validator.OfficeFormValidator;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifest;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifestEntry;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.api.offices.entity.RegionalOffice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class TablesControllerAjax {

  private static Log logger = LogFactory.getLog(TablesControllerAjax.class);

  private static final String FORMS_JSON_FILENAME = "formDef.json";

  @Autowired
  OdkClientFactory odkClientFactory;

  @GetMapping(value = "/tables/{tableId}/rows/{rowId}", produces = "application/json")
  public ResponseEntity<?> getRowDetail(@PathVariable("tableId") String tableId,
      @PathVariable(name = "rowId") String rowId, Model model) {

    OdkClient odkClient = odkClientFactory.getOdkClient();
    TableResource tableResource = odkClient.getTableResource(tableId);
    RowResource rowResource = odkClient.getSingleRow(tableId, tableResource.getSchemaETag(), rowId);

    getFormJson(odkClient, tableId);

    return ResponseEntity.ok("");
  }

  public void getFormJson(OdkClient odkClient, String tableId) {
    OdkTablesFileManifest manifest = odkClient.getTableManifest(tableId);
    OdkTablesFileManifestEntry formDefEntry = null;
    for (OdkTablesFileManifestEntry entry : manifest.getFiles()) {
      if (entry.filename != null
          && entry.filename.toLowerCase().endsWith(FORMS_JSON_FILENAME.toLowerCase())) {
        formDefEntry = entry;
        break;
      }
    }
    String jsonFormDefintion = odkClient.getFormDefinition(formDefEntry.downloadUrl);
    try {
      JsonNode rootNode = new ObjectMapper().readTree(new StringReader(jsonFormDefintion));
      final JsonNode surveyNodeList = rootNode.get("xls").get("survey");
      for (final JsonNode surveyNode : surveyNodeList) {
      SurveyQuestion surveyQuestion = new SurveyQuestion();
    }
    } catch (JsonProcessingException e) {
      logger.error(e);
    } catch (IOException e) {
      logger.error(e);
    }

  }

}
