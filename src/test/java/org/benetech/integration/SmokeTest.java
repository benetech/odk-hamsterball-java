package org.benetech.integration;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static ru.yandex.qatools.embed.postgresql.EmbeddedPostgres.cachedRuntimeConfig;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.configuration.MvcConfiguration;
import org.benetech.configuration.SecurityConfiguration;
import org.benetech.configuration.WebClientConfiguration;
import org.benetech.util.DBUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;


/**
 * There's a lot of work to do here. This is an experimental attempt at running an embedded Postgres
 * instance + the web service + the web client + selenium.
 * 
 * It's an end-to-end integration test that starts the database from scratch every time.
 * 
 * @author Caden Howell <cadenh@benetech.org>
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(
    classes = {MvcConfiguration.class, SecurityConfiguration.class, WebClientConfiguration.class})
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"integrationtest"})
public class SmokeTest {

  @Autowired
  private EmbeddedWebApplicationContext server;

  boolean environmentReady = true;

  private WebDriver webDriver;
  private WebDriverWait wait;

  private static Log logger = LogFactory.getLog(SmokeTest.class);
  final PathMatchingResourcePatternResolver pmrpr = new PathMatchingResourcePatternResolver();

  private EmbeddedPostgres postgres;
  private Process webServiceProcess;

  private static final int JDBC_PORT = 15433;
  private static final String JDBC_DATABASE = "hamster_unit";
  private static final String JDBC_USERNAME = "hamster_unit";
  private static final String JDBC_PASSWORD = "hamster_unit";
  private String postgresUrl;

  private static final String WEB_SERVICE_PORT = "11223";
  private static final String DEFAULT_ADMIN_USERNAME = "admin";
  private static final String DEFAULT_ADMIN_PASSWORD = "aggregate";


  /**
   * For our smoke test, log into the web client, and verify that we are logged in.
   * @throws IOException
   */
  @Test
  public void smoke() throws IOException {
    if (environmentReady) {
      webDriver.get("http://localhost:22334");
      WebElement usernameInput = webDriver.findElement(By.name("username"));
      usernameInput.sendKeys(DEFAULT_ADMIN_USERNAME);
      WebElement passwordInput = webDriver.findElement(By.name("password"));
      passwordInput.sendKeys(DEFAULT_ADMIN_PASSWORD);
      WebElement submitButton = webDriver.findElement(By.cssSelector("input.btn-default"));
      submitButton.click();
      wait.until(ExpectedConditions.presenceOfElementLocated(By.id("currentLoggedInUser")));

      WebElement loggedInUserSpan = webDriver.findElement(By.id("currentLoggedInUser"));
      assertThat(loggedInUserSpan.getText(), is("admin"));

      logger.info(webDriver.getPageSource());
    }
    else {
      logger.info("The integration test was skipped because your environment is not ready.");
    }
  }

  @Before
  public void setup() {
    try {
      setupPostgresServer();
      setupWebService();
      webDriver = new FirefoxDriver();
      wait = new WebDriverWait(webDriver, 10);
    } catch (IOException | InterruptedException | SQLException e) {
      logger.info(
          "Your environment isn't set up for the end-to-end integration test.  Don't panic.\nHere's the cause:");
      logger.info(e);
    }
  }

  @After
  public void destroy() throws InterruptedException, IOException {
    logger.info("Destroying...");
    logger.info("Closing webdriver");
    webDriver.close();
    logger.info("Killing web service");
    webServiceProcess.getOutputStream().close();
    webServiceProcess.getErrorStream().close();
    webServiceProcess.getInputStream().close();
    webServiceProcess.destroy();
    webServiceProcess.waitFor(10, TimeUnit.SECONDS);
    webServiceProcess.destroyForcibly();
    webServiceProcess.waitFor();
    logger.info("Killing postgres");
    postgres.stop();
    postgres.getProcess().ifPresent(PostgresProcess::stop);
  }

  private String getWebServiceJarPath() {
    StringBuilder builder = new StringBuilder(System.getProperty("user.home"));
    builder.append(File.separator).append(".m2").append(File.separator).append("repository")
        .append(File.separator).append("org").append(File.separator).append("benetech")
        .append(File.separator).append("odk-hamster").append(File.separator).append("1.0-SNAPSHOT")
        .append(File.separator).append("odk-hamster-1.0-SNAPSHOT.jar");
    String path = builder.toString();

    logger.info("Assuming web service jar is located at " + path);
    File file = new File(path);
    if (!file.exists()) {
      logger.error(
          "Well, this isn't going to work.  The web service isn't installed in your Maven repository.");
      throw new RuntimeException("No web service jar in Maven repository");
    }
    return path;
  }

  /**
   * Set up a clean new embedded Postgres server
   * 
   * @throws IOException
   * @throws SQLException
   */
  private void setupPostgresServer() throws IOException, SQLException {
    postgres = new EmbeddedPostgres();
    IRuntimeConfig runtimeConfig =
        cachedRuntimeConfig(Paths.get(System.getProperty("java.io.tmpdir"), "pgembed"));
    postgresUrl = postgres.start(runtimeConfig, "localhost", JDBC_PORT, JDBC_DATABASE,
        JDBC_USERNAME, JDBC_PASSWORD, emptyList());
    Connection connection = DriverManager.getConnection(postgresUrl, JDBC_USERNAME, JDBC_PASSWORD);
    DBUtil.setupEmptyDatabase(connection, JDBC_USERNAME, JDBC_DATABASE);
  }

  /**
   * The web service is just a Spring Boot app in a jar, so we can run it as normal.
   * 
   * @throws IOException
   * @throws InterruptedException
   */
  private void setupWebService() throws IOException, InterruptedException {
    ProcessBuilder pb = new ProcessBuilder("java", "-jar", getWebServiceJarPath());
    Map<String, String> env = pb.environment();
    env.put("SPRING_DATASOURCE_URL", postgresUrl);
    env.put("SPRING_DATASOURCE_USERNAME", JDBC_USERNAME);
    env.put("SPRING_DATASOURCE_PASSWORD", JDBC_PASSWORD);
    env.put("JDBC_SCHEMA", JDBC_DATABASE);
    env.put("SERVER_PORT", WEB_SERVICE_PORT);

    webServiceProcess = pb.start();
    BufferedReader in =
        new BufferedReader(new InputStreamReader(webServiceProcess.getInputStream()));
    String line;
    while ((line = in.readLine()) != null) {
      System.out.println(line);
      if (line.contains("Started Application in")) {
        break;
      }
    }

    inheritIO(webServiceProcess.getInputStream(), System.out);
    inheritIO(webServiceProcess.getErrorStream(), System.err);
  }

  /**
   * Once we know we've started the web service, dump the output to stdout
   * 
   * @param src
   * @param dest
   * @author Evgeniy Dorofeev
   * @see https://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki
   */
  private static void inheritIO(final InputStream src, final PrintStream dest) {
    new Thread(new Runnable() {
      public void run() {
        Scanner sc = new Scanner(src);
        while (sc.hasNextLine()) {
          dest.println(sc.nextLine());
        }
      }
    }).start();
  }

}
