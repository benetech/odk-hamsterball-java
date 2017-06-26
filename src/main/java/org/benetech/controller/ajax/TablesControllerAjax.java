package org.benetech.controller.ajax;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.ajax.AjaxFormResponse;
import org.benetech.ajax.AjaxFormResponseFactory;
import org.benetech.ajax.SurveyQuestion;
import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.model.display.OdkTablesFileManifestEntryDisplay;
import org.benetech.validator.OfficeFormValidator;
import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
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


    return ResponseEntity.ok(rowResource);
  }

  @GetMapping(value = "/tables/{tableId}/rows/{rowId}/map", produces = "application/json")
  public ResponseEntity<?> getRowDetailMap(@PathVariable("tableId") String tableId,
      @PathVariable(name = "rowId") String rowId, Model model) {

    OdkClient odkClient = odkClientFactory.getOdkClient();
    TableResource tableResource = odkClient.getTableResource(tableId);
    RowResource rowResource = odkClient.getSingleRow(tableId, tableResource.getSchemaETag(), rowId);
    Map<String, String> mappedRowValues = new HashMap<String, String>();
    for (DataKeyValue value : rowResource.getValues()) {
      if (value.column.toLowerCase().endsWith("_contentType".toLowerCase())) {
        // skip
      } else if (value.column.toLowerCase().endsWith("_urifragment")) {
        String origColumnName =
            value.column.substring(0, value.column.length() - "_uriFragment".length());
        mappedRowValues.put(origColumnName, value.value);
      } else {
        mappedRowValues.put(value.column, value.value);
      }
    }

    return ResponseEntity.ok(mappedRowValues);
  }

  @GetMapping(value = "/tables/{tableId}/rows/{rowId}/attachments", produces = "application/json")
  public ResponseEntity<?> getRowAttachments(@PathVariable("tableId") String tableId,
      @PathVariable(name = "rowId") String rowId, Model model) {

    OdkClient odkClient = odkClientFactory.getOdkClient();
    TableResource tableResource = odkClient.getTableResource(tableId);
    OdkTablesFileManifest manifest =
        odkClient.getSingleRowAttachments(tableId, tableResource.getSchemaETag(), rowId);

    Map<String, OdkTablesFileManifestEntry> entryMap =
        new HashMap<String, OdkTablesFileManifestEntry>();
    for (OdkTablesFileManifestEntry entry : manifest.getFiles()) {
      entryMap.put(entry.filename, new OdkTablesFileManifestEntryDisplay(entry));
    }

    return ResponseEntity.ok(entryMap);
  }

  @GetMapping(value = "/tables/{tableId}/questions", produces = "application/json")
  public ResponseEntity<?> getFormJson(@PathVariable("tableId") String tableId) {
    OdkClient odkClient = odkClientFactory.getOdkClient();

    OdkTablesFileManifest manifest = odkClient.getTableManifest(tableId);
    OdkTablesFileManifestEntry formDefEntry = null;
    for (OdkTablesFileManifestEntry entry : manifest.getFiles()) {
      if (entry.filename != null
          && entry.filename.toLowerCase().endsWith(FORMS_JSON_FILENAME.toLowerCase())) {
        formDefEntry = entry;
        break;
      }
    }
    String jsonFormDefinition = odkClient.getFormDefinition(formDefEntry.downloadUrl);
    Map<String, SurveyQuestion> surveyQuestionMap = new HashMap<String, SurveyQuestion>();
    try {
      JsonNode rootNode = new ObjectMapper().readValue(jsonFormDefinition, JsonNode.class);
      surveyQuestionMap = getSurveyQuestionMap(rootNode);
      logger.info("jsonFormDefinition:\n" + jsonFormDefinition);

      
    } catch (JsonProcessingException e) {
      logger.error(e);
    } catch (IOException e) {
      logger.error(e);
    }

    return ResponseEntity.ok(surveyQuestionMap);

  }
  
  Map<String, SurveyQuestion> getSurveyQuestionMap(JsonNode rootNode)
  {
    Map<String, SurveyQuestion> surveyQuestionMap = new HashMap<String, SurveyQuestion>();
    final JsonNode xlsNode = rootNode.path("xlsx");
    Iterator<JsonNode> surveyLevelChildren = xlsNode.iterator();
    while (surveyLevelChildren.hasNext()) {
      JsonNode surveyLevelChild = surveyLevelChildren.next();
      Iterator<JsonNode> questionLevelChildren = surveyLevelChild.iterator();
      while (questionLevelChildren.hasNext()) {
        JsonNode questionLevelChild = questionLevelChildren.next();
        List<JsonNode> questionNodes = getQuestionNodes(questionLevelChild);
        for (final JsonNode questionNode : questionNodes) {
          SurveyQuestion surveyQuestion = new SurveyQuestion();
          surveyQuestion.setName(getTextNullSafe(questionNode, "name", "_ERROR_DEFAULT"));
          surveyQuestion.setDisplayText(getDisplayTextNullSafe(questionNode));
          surveyQuestion.setType(getTextNullSafe(questionNode, "type", ""));
          surveyQuestion.setRowNum(getIntNullSafe(questionNode, "_row_num"));
          surveyQuestionMap.put(surveyQuestion.getName(), surveyQuestion);
        }
      }
    }
    return surveyQuestionMap;
  }

  /**
   * Search deeply for nodes that are probably questions (with name and display nodes)
   * 
   * @param surveyNode
   * @return
   */
  List<JsonNode> getQuestionNodes(JsonNode surveyNode) {
    List<JsonNode> result = new ArrayList<JsonNode>();
    List<JsonNode> nodesToVisit = new ArrayList<JsonNode>();
    nodesToVisit.add(surveyNode);
    while (!nodesToVisit.isEmpty()) {
      JsonNode currentNode = nodesToVisit.remove(0);
      JsonNode nameNode = currentNode.get("name");
      JsonNode displayTextNode = currentNode.get("display");
      if (nameNode == null || nameNode.isNull() || displayTextNode == null
          || displayTextNode.isNull()) {
        Iterator<JsonNode> children = currentNode.iterator();
        while (children.hasNext()) {
          JsonNode child = children.next();
          nodesToVisit.add(child);
        }
      } else {
        result.add(currentNode);
      }
    }
    return result;
  }



  private int getIntNullSafe(JsonNode node, String path) {
    int result = 0;
    if (node.findPath(path) != null && !node.findPath(path).isNull()) {
      result = node.findPath(path).asInt();
    }
    return result;
  }

  String getTextNullSafe(JsonNode node, String path, String defaultText) {
    String result = "";
    if (node.findPath(path) == null || node.findPath(path).isNull()) {
      result = defaultText;
    } else {
      result = node.findPath(path).asText();
    }
    return result;
  }

  String getDisplayTextNullSafe(JsonNode node) {
    String result = "";
    JsonNode displayNode = node.findPath("display");

    if (displayNode == null || displayNode.isNull()) {
      result = "";
    } else {
      JsonNode textNode = displayNode.get("text");
      JsonNode imageNode = displayNode.get("image");
      if (textNode != null && !textNode.isNull()) {
        JsonNode defaultNode = textNode.get("default");
        if (defaultNode != null && !defaultNode.isNull()) {
          result = defaultNode.asText();
          Iterator<Entry<String, JsonNode>> children = textNode.fields();
          while (children.hasNext()) {
            Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) children.next();
            logger.info(entry.getKey() + " " + entry.getValue());
            if (!entry.getKey().equalsIgnoreCase("default")) {
              JsonNode childNode = entry.getValue();
              if (childNode != null && !childNode.isNull()) {
                result = result + " / " + childNode.asText();
              }
            }
          }
        } else {
          result = textNode.asText();
        }
      } else if (imageNode != null && !imageNode.isNull()) {
        result = imageNode.asText();
      } else {
        result = "";
      }
    }
    return result;
  }


}
