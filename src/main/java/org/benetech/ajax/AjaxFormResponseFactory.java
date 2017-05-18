package org.benetech.ajax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * Decouple the validation object from Spring constructs into simple collections that can be
 * serialized into JSON.
 * 
 * @author Caden Howell <cadenh@benetech.org>
 *
 */
@Component
public class AjaxFormResponseFactory {

  @Autowired
  private MessageSource messageSource;

  public AjaxFormResponse getAjaxFormResponse(BindingResult bindingResult, String summaryMessage) {

    Map<String, List<String>> errors = new HashMap<String, List<String>>();
    for (ObjectError objectError : bindingResult.getAllErrors()) {

      if (objectError instanceof FieldError) {
        FieldError fieldError = (FieldError) objectError;
        String message = messageSource.getMessage(fieldError, null);

        if (errors.containsKey(fieldError.getField())) {
          errors.get(fieldError.getField()).add(message);
        } else {
          List<String> errorList = new ArrayList<String>();
          errorList.add(message);
          errors.put(fieldError.getField(), errorList);
        }

      } else {
        String message = messageSource.getMessage(objectError, null);

        if (errors.containsKey(objectError.getObjectName())) {
          errors.get(objectError.getObjectName()).add(message);
        } else {
          List<String> errorList = new ArrayList<String>();
          errorList.add(message);
          errors.put(objectError.getObjectName(), errorList);
        }
      }
    }
    return new AjaxFormResponse(errors, summaryMessage);

  }
}
