package uk.co.ogauthority.pwa.repository.pipelines;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class CountPipelineDetailsForPipelineDtoTest {

  @Test
  public void testEqualsAndHashCode() {
    EqualsVerifier.forClass(CountPipelineDetailsForPipelineDto.class)
        .verify();

  }
}