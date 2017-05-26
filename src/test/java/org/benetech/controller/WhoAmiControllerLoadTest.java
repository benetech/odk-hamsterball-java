package org.benetech.controller;

import org.junit.Test;
import static org.junit.Assert.assertThat;

import org.benetech.configuration.annotations.UnitTestConfig;

import static org.hamcrest.Matchers.notNullValue;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@UnitTestConfig
@SpringBootTest
public class WhoAmiControllerLoadTest {

  @Autowired
  private WhoAmIController controller;

  @Test
  public void contextLoads() throws Exception {
    assertThat(controller,notNullValue());
    
  }

}
