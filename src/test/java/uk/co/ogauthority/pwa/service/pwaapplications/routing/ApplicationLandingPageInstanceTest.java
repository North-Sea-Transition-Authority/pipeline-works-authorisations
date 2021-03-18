package uk.co.ogauthority.pwa.service.pwaapplications.routing;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ApplicationLandingPageInstanceTest {

  @Test
  public void testEqualsAndHashCode() {
    EqualsVerifier.forClass(ApplicationLandingPageInstance.class)
        .verify();
  }
}