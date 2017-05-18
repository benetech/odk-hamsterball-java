package org.benetech.ajax;

import java.util.List;
import java.util.Map;

/**
 * Decouple the validation object created by Spring from Spring constructs into simple collections
 * that can be serialized into JSON.
 * 
 * @author Caden Howell <cadenh@benetech.org>
 *
 */
public class AjaxFormResponse {

  private Map<String, List<String>> errors;
  private String summaryMessage;


  public AjaxFormResponse(Map<String, List<String>> errors, String summaryMessage) {
    this.errors = errors;
    this.summaryMessage = summaryMessage;
  }

  public AjaxFormResponse(String summaryMessage) {
    this.summaryMessage = summaryMessage;
  }

  public String getSummaryMessage() {
    return summaryMessage;
  }

  public void setSummaryMessage(String summaryMessage) {
    this.summaryMessage = summaryMessage;
  }

  public Map<String, List<String>> getErrors() {
    return errors;
  }

  public void setErrors(Map<String, List<String>> errors) {
    this.errors = errors;
  }

}
