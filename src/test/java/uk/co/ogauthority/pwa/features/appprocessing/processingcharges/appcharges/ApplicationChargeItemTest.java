package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ApplicationChargeItemTest {

  @Test
  public void testEqualsAndHashCode() {
    EqualsVerifier.forClass(ApplicationChargeItem.class)
        .verify();
  }
}