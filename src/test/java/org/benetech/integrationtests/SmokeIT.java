package org.benetech.integrationtests;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static ru.yandex.qatools.embed.postgresql.EmbeddedPostgres.cachedRuntimeConfig;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.ActiveProfiles;
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
 * It assumes you have the server jar already installed in your Maven repo
 * and your Maven repository is in the default (linux) location
 * 
 * This would probably be best moved into its own project dependent on the client and service jars
 * so that the client and web service are better treated as black boxes.
 * 
 * @author Caden Howell <cadenh@benetech.org>
 */
@RunWith(SpringRunner.class)
@ActiveProfiles({"integrationtest"})
public class SmokeIT {

  boolean environmentReady = true;

  private WebDriver webDriver;
  private WebDriverWait wait;

  private static Log logger = LogFactory.getLog(SmokeIT.class);
  final PathMatchingResourcePatternResolver pmrpr = new PathMatchingResourcePatternResolver();

  private EmbeddedPostgres postgres;
  private Process webServiceProcess;
  private Process webClientProcess;

  private static final int JDBC_PORT = 15433;
  private static final String JDBC_DATABASE = "hamster_unit";
  private static final String JDBC_USERNAME = "hamster_unit";
  private static final String JDBC_PASSWORD = "hamster_unit";
  private String postgresUrl;

  private static final String WEB_SERVICE_PORT = "11223";
  private static final String WEB_CLIENT_PORT = "22334";
  private static final String WEB_SERVICE_VERSION = "1.0-SNAPSHOT";
  private static final String WEB_CLIENT_VERSION = "1.0.0-SNAPSHOT";
  private static final String DEFAULT_ADMIN_USERNAME = "admin";
  private static final String DEFAULT_ADMIN_PASSWORD = "aggregate";
  private static final String ODK_URL = "http://localhost:11223";


  /**
   * For our smoke test, log into the web client, and verify that we are logged in.
   * 
   * @throws IOException
   */
  @Test
  public void smokeTest() throws IOException {
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
    } else {
      logger.info("The integration test was skipped because your environment is not ready.");
    }
  }

  @Before
  public void setup() {
    try {
      setupPostgresServer();
      String webServicePath = getWebServiceJarPath();
      String webClientPath = getWebClientJarPath();
      setupWebService(webServicePath);
      setupWebClient(webClientPath);
      webDriver = new FirefoxDriver();
      wait = new WebDriverWait(webDriver, 10);
    } catch (Throwable e) {
      logger.info(
          "Your environment isn't set up for the end-to-end integration test.  Don't panic.\nHere's the cause:");
      logger.info(e);
      environmentReady = false;
    }
  }

  @After
  public void destroy() throws InterruptedException, IOException {
    logger.info("Destroying...");
    logger.info("Closing webdriver");
    webDriver.close();
    logger.info("Killing web service");
    killProcess(webServiceProcess);
    logger.info("Killing web client");
    killProcess(webClientProcess);
    logger.info("Killing postgres");
    postgres.stop();
    postgres.getProcess().ifPresent(PostgresProcess::stop);
  }
  
  private void killProcess(Process process) throws IOException, InterruptedException {
    process.getOutputStream().close();
    process.getErrorStream().close();
    process.getInputStream().close();
    process.destroy();
    process.waitFor(10, TimeUnit.SECONDS);
    process.destroyForcibly();
    process.waitFor();
  }

  /**
   * Get the path to the built web service jar in the default maven repo location.
   * @return path
   */
  // TODO: Can we extract this and client jar path from Maven?
  private String getWebServiceJarPath() {
    StringBuilder builder = new StringBuilder(System.getProperty("user.home"));
    builder.append(File.separator).append(".m2").append(File.separator).append("repository")
        .append(File.separator).append("org").append(File.separator).append("benetech")
        .append(File.separator).append("odk-hamster").append(File.separator)
        .append(WEB_SERVICE_VERSION).append(File.separator)
        .append("odk-hamster-" + WEB_SERVICE_VERSION + ".jar");
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
   * Get the path to the built web client jar in the default maven repo location.
   * @return path
   */
  private String getWebClientJarPath() {
    StringBuilder builder = new StringBuilder(System.getProperty("user.home"));
    builder.append(File.separator).append(".m2").append(File.separator).append("repository")
        .append(File.separator).append("org").append(File.separator).append("benetech")
        .append(File.separator).append("odk-hamsterball-java").append(File.separator)
        .append(WEB_CLIENT_VERSION).append(File.separator)
        .append("odk-hamsterball-java-" + WEB_CLIENT_VERSION + ".jar");
    String path = builder.toString();

    logger.info("Assuming web service jar is located at " + path);
    File file = new File(path);
    if (!file.exists()) {
      logger.error(
          "Well, this isn't going to work.  The web client isn't installed in your Maven repository.");
      throw new RuntimeException("No web client jar in Maven repository");
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
  private void setupWebService(String path) throws IOException, InterruptedException {
    ProcessBuilder pb = new ProcessBuilder("java", "-jar", path);
    Map<String, String> env = pb.environment();
    env.put("SPRING_DATASOURCE_URL", postgresUrl);
    env.put("SPRING_DATASOURCE_USERNAME", JDBC_USERNAME);
    env.put("SPRING_DATASOURCE_PASSWORD", JDBC_PASSWORD);
    env.put("JDBC_SCHEMA", JDBC_DATABASE);
    env.put("SERVER_PORT", WEB_SERVICE_PORT);

    webServiceProcess = pb.start();
    watchProcessLaunch(webServiceProcess);
  }

  private void setupWebClient(String path) throws IOException {
    ProcessBuilder pb = new ProcessBuilder("java", "-jar", path);
    Map<String, String> env = pb.environment();
    env.put("ODK_URL", ODK_URL);
    env.put("SERVER_PORT", WEB_CLIENT_PORT);

    webClientProcess = pb.start();
    watchProcessLaunch(webClientProcess);
  }

  
  private void watchProcessLaunch(Process process) throws IOException {
    BufferedReader in =
        new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = in.readLine()) != null) {
      System.out.println(line);
      if (line.contains("Started Application in")) {
        break;
      }
    }

    inheritIO(process.getInputStream(), System.out);
    inheritIO(process.getErrorStream(), System.err);
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
