package org.benetech.model.form;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.benetech.constants.GeneralConsts;
import org.benetech.util.UserUtils;
import org.opendatakit.api.users.entity.UserEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserEntityForm extends UserEntity {
  
  String username;

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
    super(userEntity.getUserId(), userEntity.getFullName(), userEntity.getOfficeId(),
        userEntity.getRoles());
  }

  public UserEntity getUserEntity() {
    return new UserEntity(this.getUserId(), this.getFullName(), this.getOfficeId(),
        this.getRoles());
  }

  /**
   * Set the username from the user id if available and return the username
   * @return
   */
  public String getUsername() {
    if (StringUtils.isEmpty(username) && StringUtils.isNotEmpty(getUserId())) {
      username = UserUtils.idToUsername(getUserId());
    }
    return username;
  }

  /** 
   * Set the username and update the user ID.
   * @param username
   */
  public void setUsername(String username) {
    this.username = username;
    // We can't change anonymous username.
    if (!username.equals(GeneralConsts.ANONYMOUS_USERNAME)) {
      setUserId(UserUtils.usernameToId(username));
    }
  }
}
