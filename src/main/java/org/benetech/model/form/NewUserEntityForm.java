package org.benetech.model.form;

import java.util.List;

import org.opendatakit.api.users.entity.UserEntity;

public class NewUserEntityForm extends UserEntityForm {

  ChangePasswordForm changePasswordForm;



  public NewUserEntityForm() {
    super();
    changePasswordForm = new ChangePasswordForm();
  }

  public NewUserEntityForm(String user_id, String full_name, String officeId, List<String> roles) {
    super(user_id, full_name, officeId, roles);
    changePasswordForm = new ChangePasswordForm();
  }

  public NewUserEntityForm(String user_id, String full_name, String officeId, String... roles) {
    super(user_id, full_name, officeId, roles);
    changePasswordForm = new ChangePasswordForm();
  }

  public NewUserEntityForm(UserEntity userEntity) {
    super(userEntity);
    changePasswordForm = new ChangePasswordForm();
  }

  public ChangePasswordForm getChangePasswordForm() {
    return changePasswordForm;
  }

  public void setChangePasswordForm(ChangePasswordForm changePasswordForm) {
    this.changePasswordForm = changePasswordForm;
  }

  public String getPassword1() {
    return changePasswordForm.getPassword1();
  }

  public void setPassword1(String password1) {
    changePasswordForm.setPassword1(password1);;
  }

  public String getPassword2() {
    return changePasswordForm.getPassword2();
  }

  public void setPassword2(String password2) {
    changePasswordForm.setPassword2(password2);
  }

}
