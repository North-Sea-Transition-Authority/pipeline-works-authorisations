package uk.co.ogauthority.pwa.service.search.applicationsearch;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class ApplicationSearchParametersTest {

  @Test
  public void testEquals() {
    // value object but class needs to be mutable for Spring binding so suppress warning.
    EqualsVerifier.forClass(ApplicationSearchParameters.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();

  }
}