package uk.co.ogauthority.pwa.model.dto.pipelines;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class PipelineDetailIdTest {

  @Test
  public void testEqualsAndHashcode() {
    EqualsVerifier.forClass(PipelineDetailId.class)
        .verify();

  }
}