package org.benetech.controller.ajax;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@ActiveProfiles({"unittest"})
public class TablesControllerAjaxTest {
  private ObjectMapper mapper = new ObjectMapper();
  TablesControllerAjax controller;
  Log logger = LogFactory.getLog(TablesControllerAjaxTest.class);


  @Before
  public void init() {
    controller = new TablesControllerAjax();
  }

  @Test
  public void parseDisplayTextTest() throws Exception {
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
    logger.info("Display Text: " + result);
  }

  @Test
  public void parseDisplayTextMultiLangTest() throws Exception {
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

    logger.info("Display Text: " + result);
  }
}
