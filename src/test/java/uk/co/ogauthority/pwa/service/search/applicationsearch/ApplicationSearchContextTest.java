package uk.co.ogauthority.pwa.service.search.applicationsearch;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ApplicationSearchContextTest {

  @Test
  public void testEquals() {

    EqualsVerifier.forClass(ApplicationSearchContext.class)
        .verify();
  }
}