package uk.co.ogauthority.pwa.model.dto.pipelines;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class PipelineDetailIdTest {

  @Test
  void equalsAndHashcode() {
    EqualsVerifier.forClass(PipelineDetailId.class)
        .verify();

  }
}