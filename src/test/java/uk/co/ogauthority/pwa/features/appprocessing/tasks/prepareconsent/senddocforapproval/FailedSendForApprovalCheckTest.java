package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class FailedSendForApprovalCheckTest {

  @Test
  void equalsAndHashCode() {
    EqualsVerifier.forClass(FailedSendForApprovalCheck.class)
        .verify();
  }
}