package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ApplicationChargeItemTest {

  @Test
  void equalsAndHashCode() {
    EqualsVerifier.forClass(ApplicationChargeItem.class)
        .verify();
  }
}