package org.benetech.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.benetech.model.form.UserEntityForm;
import org.opendatakit.api.offices.entity.RegionalOffice;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class OfficeFormValidator implements Validator {
  
  private static final String OFFICE_ID_PATTERN_STRING = "[a-z]+";
  private static final Pattern OFFICE_ID_PATTERN = Pattern.compile(OFFICE_ID_PATTERN_STRING);


  @Override
  public boolean supports(Class<?> clazz) {
    return RegionalOffice.class.equals(clazz);

  }

  @Override
  public void validate(Object target, Errors errors) {

    RegionalOffice office = (RegionalOffice) target;

    if (StringUtils.isEmpty(office.getName())) {
      errors.rejectValue("name", "empty");
    }
    if (StringUtils.isEmpty(office.getOfficeId())) {
      errors.rejectValue("officeId", "empty");
    }
    Matcher matcher = OFFICE_ID_PATTERN.matcher(office.getOfficeId());
    if (!matcher.matches()) {
      errors.rejectValue("officeId", "wrongPattern");
    }
  }

}
