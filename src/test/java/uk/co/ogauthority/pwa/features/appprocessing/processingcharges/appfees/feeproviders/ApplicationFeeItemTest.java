package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.feeproviders;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ApplicationFeeItemTest {

  @Test
  void equalsAndHashcode() {

    EqualsVerifier.forClass(ApplicationFeeItem.class)
        .verify();
  }
}