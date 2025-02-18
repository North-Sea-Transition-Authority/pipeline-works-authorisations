package uk.co.ogauthority.pwa.service.pwaapplications.routing;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ApplicationLandingPageInstanceTest {

  @Test
  void equalsAndHashCode() {
    EqualsVerifier.forClass(ApplicationLandingPageInstance.class)
        .verify();
  }
}