package org.benetech.integration;

import static java.util.Collections.emptyList;
//import static ru.yandex.qatools.embed.postgresql.EmbeddedPostgres.cachedRuntimeConfig;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.junit4.SpringRunner;

//import de.flapdoodle.embed.process.config.IRuntimeConfig;
//import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

@RunWith(SpringRunner.class)
public class SmokeTest {

  @Autowired
  private EmbeddedWebApplicationContext server;

  private static Log logger = LogFactory.getLog(SmokeTest.class);
  final PathMatchingResourcePatternResolver pmrpr = new PathMatchingResourcePatternResolver();

//  private EmbeddedPostgres postgres;
//
//  String JDBC_DATABASE = "hamster_unit";
//  String JDBC_USERNAME = "hamster_unit";
//  String JDBC_PASSWORD = "hamster_unit";
//  String JDBC_URL = "jdbc:postgresql://localhost:15433/hamster_unit?autoDeserialize=true";
//  int JDBC_PORT = 15433;
//
//  String url;
//
//  @Test
//  public void smoke() throws IOException {
//    postgres = new EmbeddedPostgres();
//    IRuntimeConfig runtimeConfig =
//        cachedRuntimeConfig(Paths.get(System.getProperty("java.io.tmpdir"), "pgembed"));
//    url = postgres.start(runtimeConfig, "localhost", JDBC_PORT, JDBC_USERNAME, JDBC_PASSWORD,
//        "not the real password", emptyList());
//
//  }



}
