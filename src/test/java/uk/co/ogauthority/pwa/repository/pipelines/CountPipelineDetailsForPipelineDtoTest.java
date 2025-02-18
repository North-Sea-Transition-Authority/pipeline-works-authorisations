package uk.co.ogauthority.pwa.repository.pipelines;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class CountPipelineDetailsForPipelineDtoTest {

  @Test
  void equalsAndHashCode() {
    EqualsVerifier.forClass(CountPipelineDetailsForPipelineDto.class)
        .verify();

  }
}