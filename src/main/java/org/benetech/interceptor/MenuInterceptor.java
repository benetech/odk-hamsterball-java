package org.benetech.interceptor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.constants.GeneralConsts;
import org.benetech.util.OdkClientUtils;
import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class MenuInterceptor extends HandlerInterceptorAdapter {

  @Autowired
  private OdkClientFactory odkClientFactory;

  private static final Log logger = LogFactory.getLog(HandlerInterceptorAdapter.class);

  @Override
  public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
      final Object handler, final ModelAndView modelAndView) throws Exception {

    logger.info("Applying MenuInterceptor to " + request.getRequestURI());
    if (OdkClientUtils.getRestTemplate() != null && modelAndView != null) {
      OdkClient odkClient = odkClientFactory.getOdkClient();
      List<String> tableIds = odkClient.getTableIds();
      modelAndView.getModelMap().addAttribute("tableIds", tableIds);
      UserEntity user = odkClient.getCurrentUser();
      modelAndView.getModelMap().addAttribute("currentUser", user);

    }
  }

  public void setOdkClientFactory(OdkClientFactory odkClientFactory) {
    this.odkClientFactory = odkClientFactory;
  }

}
