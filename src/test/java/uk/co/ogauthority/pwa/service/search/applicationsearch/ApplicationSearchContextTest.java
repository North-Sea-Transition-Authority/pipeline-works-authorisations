package uk.co.ogauthority.pwa.service.search.applicationsearch;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ApplicationSearchContextTest {

  @Test
  void equals() {

    EqualsVerifier.forClass(ApplicationSearchContext.class)
        .verify();
  }
}