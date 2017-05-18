package org.benetech.validator;

import org.apache.commons.lang3.StringUtils;
import org.benetech.model.form.ChangePasswordForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ChangePasswordFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ChangePasswordForm.class.equals(clazz);

  }

  @Override
  public void validate(Object target, Errors errors) {

    ChangePasswordForm passwordForm = (ChangePasswordForm) target;

    if (StringUtils.isEmpty(passwordForm.getPassword1())) {
      errors.rejectValue("password1", "empty");
    }
    if (StringUtils.isEmpty(passwordForm.getPassword1())) {
      errors.rejectValue("password2", "empty");
    }
    if (!StringUtils.equals(passwordForm.getPassword1(), passwordForm.getPassword2())) {
      errors.rejectValue("password2", "match");
    }
  }

}
