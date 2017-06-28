package org.benetech.controller.ajax;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import static org.hamcrest.Matchers.isEmptyString;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.ajax.SurveyQuestion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@ActiveProfiles({"unittest"})
public class TablesControllerAjaxTest {
  private ObjectMapper mapper = new ObjectMapper();
  TablesControllerAjax controller;
  private final Log logger = LogFactory.getLog(TablesControllerAjaxTest.class);
  private final PathMatchingResourcePatternResolver pmrpr = new PathMatchingResourcePatternResolver();


  @Before
  public void init() {
    controller = new TablesControllerAjax();
  }

  @Test
  public void testParseDisplayText() throws Exception {
    logger.info("parseDisplayTextTest");

    StringBuilder jsonBuilder = new StringBuilder();
    jsonBuilder.append("{");
    jsonBuilder.append("    \"type\": \"integer\",");
    jsonBuilder.append("    \"name\": \"Salary\",");
    jsonBuilder.append("    \"display\": {");
    jsonBuilder.append("      \"text\": \"Your salary?\"},");

    jsonBuilder.append("    \"required\": false,");
    jsonBuilder.append("    \"validation_tags\": \"user_info\",");
    jsonBuilder.append("    \"_row_num\": 13");
    jsonBuilder.append("  }");

    JsonNode node = mapper.readTree(jsonBuilder.toString());

    String result = controller.getDisplayTextNullSafe(node);
    assertThat(result, is("Your salary?"));
  }

  @Test
  public void testParseDisplayTextMultiLang() throws Exception {
    logger.info("parseDisplayTextMultiLangTest");

    StringBuilder jsonBuilder = new StringBuilder();
    jsonBuilder.append("{");
    jsonBuilder.append("    \"type\": \"integer\",");
    jsonBuilder.append("    \"name\": \"Salary\",");
    jsonBuilder.append("    \"display\": {");
    jsonBuilder.append("      \"text\": {");
    jsonBuilder.append("        \"default\": \"English words\",");
    jsonBuilder.append("        \"spanish\": \"Spanish words\"");
    jsonBuilder.append("     }");
    jsonBuilder.append("    },");
    jsonBuilder.append("    \"required\": false,");
    jsonBuilder.append("    \"validation_tags\": \"user_info\",");
    jsonBuilder.append("    \"_row_num\": 13");
    jsonBuilder.append("  }");

    JsonNode node = mapper.readTree(jsonBuilder.toString());

    String result = controller.getDisplayTextNullSafe(node);
    assertThat(result, is("English words / Spanish words"));

  }
  
  @Test
  public void testParseDeepQuestions() throws Exception {
    logger.info("parseDisplayTextTest");
    final File sampleForm = pmrpr.getResource("classpath:formDef/madison_sample_formDef.json").getFile();

    String formDef = FileUtils.readFileToString(sampleForm);

    JsonNode node = mapper.readTree(formDef);

    List<JsonNode> result = controller.getQuestionNodes(node);

    for (JsonNode resultNode : result) {
      String name = resultNode.get("name").asText();
      assertThat(name, not(isEmptyString()));
      JsonNode display = resultNode.get("display");
      assertThat(display, is(not(nullValue())));
      JsonNode text = display.get("text");
      assertThat(display, is(not(nullValue())));
      // This particular example file has default and Spanish text
      String spanish = text.get("spanish").asText();
      assertThat(spanish, not(isEmptyString()));
      String defaultText = text.get("default").asText();
      assertThat(defaultText, not(isEmptyString()));

      logger.info(name + ": " + defaultText + "/" + spanish);

    }
  }

  @Test
  public void testGetSurveyQuestionMap() throws Exception {
    logger.info("parseDisplayTextTest");
    final File sampleForm = pmrpr.getResource("classpath:formDef/madison_sample_formDef.json").getFile();

    String formDef = FileUtils.readFileToString(sampleForm);

    JsonNode node = mapper.readTree(formDef);

    Map<String, SurveyQuestion> result = controller.getSurveyQuestionMap(node);

    assertThat(result.keySet(), containsInAnyOrder("Earners", "Salary", "WeeklySales", "Secondary", "FamilySize", "Unpaid", "WeeklyExpenses", "picture", "Area", "stable", "location", "Children", "credit", "Principal"));

  }
  
}
