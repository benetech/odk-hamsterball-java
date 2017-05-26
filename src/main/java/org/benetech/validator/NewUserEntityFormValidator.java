package org.benetech.validator;

import org.benetech.model.form.NewUserEntityForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Combines new user information with initial setting of password.
 */
@Component
public class NewUserEntityFormValidator extends UserEntityFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return NewUserEntityForm.class.equals(clazz);

  }

  @Override
  public void validate(Object target, Errors errors) {

    super.validate(target, errors);
    NewUserEntityForm passwordForm = (NewUserEntityForm) target;

    ChangePasswordFormValidator passwordValidator = new ChangePasswordFormValidator();

    passwordValidator.validate(passwordForm.getChangePasswordForm(), errors);

  }
}
