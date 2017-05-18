package org.benetech.util;

import org.benetech.constants.GeneralConsts;

public class UserUtils {

  public static String idToUsername(String id) {
    String username = id;
    if (id.startsWith(GeneralConsts.USERNAME_COLON)) {
      username = id.substring(GeneralConsts.USERNAME_COLON.length());
    }
    return username;
  }
  
  public static String usernameToId(String username) {
    if (!username.equals("anonymous")) {
      return (GeneralConsts.USERNAME_COLON + username);
    }
    return username;
  }
}
