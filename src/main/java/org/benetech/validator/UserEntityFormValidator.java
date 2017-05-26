package org.benetech.validator;

import org.apache.commons.lang3.StringUtils;
import org.benetech.model.form.UserEntityForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserEntityFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return UserEntityForm.class.equals(clazz);

  }

  @Override
  public void validate(Object target, Errors errors) {

    UserEntityForm user = (UserEntityForm) target;

    if (StringUtils.isEmpty(user.getUsername())) {
      errors.rejectValue("username", "empty");
    }
    if (StringUtils.isEmpty(user.getFullName())) {
      errors.rejectValue("fullName", "empty");
    }
    if (StringUtils.isEmpty(user.getOfficeId())) {
      errors.rejectValue("officeId", "empty");
    }
  }

}
