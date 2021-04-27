package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class FailedSendForApprovalCheckTest {

  @Test
  public void testEqualsAndHashCode() {
    EqualsVerifier.forClass(FailedSendForApprovalCheck.class)
        .verify();
  }
}