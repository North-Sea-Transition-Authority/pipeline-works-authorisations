package uk.co.ogauthority.pwa.service.workarea.viewentities;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class EmbeddedWorkAreaApplicationLifecycleEventIdTest {

  @Test
  public void testEqualsAndHashcode() {
    EqualsVerifier.forClass(EmbeddedWorkAreaApplicationLifecycleEventId.class)
        .verify();
  }
}