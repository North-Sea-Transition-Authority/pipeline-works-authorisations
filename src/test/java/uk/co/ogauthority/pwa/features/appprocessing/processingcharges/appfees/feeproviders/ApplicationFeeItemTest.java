package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.feeproviders;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ApplicationFeeItemTest {

  @Test
  public void testEqualsAndHashcode() {

    EqualsVerifier.forClass(ApplicationFeeItem.class)
        .verify();
  }
}