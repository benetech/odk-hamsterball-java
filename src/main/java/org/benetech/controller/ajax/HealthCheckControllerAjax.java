package org.benetech.controller.ajax;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckControllerAjax {

  private static Log logger = LogFactory.getLog(HealthCheckControllerAjax.class);

  @GetMapping(value = "/healthcheck", produces = "application/json")
  public ResponseEntity<?> healthCheck() {

    return ResponseEntity.ok("Health Check OK");
  }
}
