package org.benetech.client;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles({"unittest"})
public class OdkUploadClientTest {
  Log logger = LogFactory.getLog(OdkUploadClientTest.class);

  @Test
  public void testParseNonce() {
    String header =
        "Digest realm=\"opendatakit.org ODK 2.0 Server\", qop=\"auth\", nonce=\"MTQ5ODY5OTgyMzQ1NzphZjRmMDk3YTczN2FlZjllYTE0ZmUyNDZmZmY5YWU4NQ==\"";

    String nonce = OdkUploadClient.parseNonce(header);

    logger.info("nonce: " + nonce);

    assertThat(nonce, is("MTQ5ODY5OTgyMzQ1NzphZjRmMDk3YTczN2FlZjllYTE0ZmUyNDZmZmY5YWU4NQ=="));
  }



}
