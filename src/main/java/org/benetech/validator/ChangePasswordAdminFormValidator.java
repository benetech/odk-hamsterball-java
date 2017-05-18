package org.benetech.validator;

import org.apache.commons.lang3.StringUtils;
import org.benetech.model.form.ChangePasswordAdminForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ChangePasswordAdminFormValidator extends ChangePasswordFormValidator
    implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ChangePasswordAdminForm.class.equals(clazz);

  }

  @Override
  public void validate(Object target, Errors errors) {

    super.validate(target, errors);
    ChangePasswordAdminForm passwordForm = (ChangePasswordAdminForm) target;

    if (StringUtils.isEmpty(passwordForm.getUsername())) {
      errors.rejectValue("username", "empty");
    }
  }

}
