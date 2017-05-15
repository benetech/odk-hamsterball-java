package org.benetech.model.form;

import java.util.List;

import org.benetech.constants.GeneralConsts;
import org.opendatakit.api.users.entity.UserEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserEntityForm extends UserEntity {

  
  
  public UserEntityForm() {
    super();
  }

  public UserEntityForm(String user_id, String full_name, String officeId, List<String> roles) {
    super(user_id, full_name, officeId, roles);
  }

  public UserEntityForm(String user_id, String full_name, String officeId, String... roles) {
    super(user_id, full_name, officeId, roles);
  }

  public UserEntityForm(UserEntity userEntity) {
    super(userEntity.getUserId(), userEntity.getFullName(), userEntity.getOfficeId(), userEntity.getRoles());
  }

  @JsonIgnore
  public String getUsername() {
    String username = getUserId();
    if (getUserId().startsWith(GeneralConsts.USERNAME_COLON)) {
      username = getUserId().substring(GeneralConsts.USERNAME_COLON.length());
    }
    return username;
  }

  @JsonIgnore
  public void setUsername(String username) {
    // We can't change anonymous username.
    if (!username.equals("anonymous")) {
      setUserId(GeneralConsts.USERNAME_COLON + username);
    }
  }
}
