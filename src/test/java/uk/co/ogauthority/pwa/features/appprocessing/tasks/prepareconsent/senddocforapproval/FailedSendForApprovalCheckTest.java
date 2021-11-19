package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class FailedSendForApprovalCheckTest {

  @Test
  public void testEqualsAndHashCode() {
    EqualsVerifier.forClass(FailedSendForApprovalCheck.class)
        .verify();
  }
}