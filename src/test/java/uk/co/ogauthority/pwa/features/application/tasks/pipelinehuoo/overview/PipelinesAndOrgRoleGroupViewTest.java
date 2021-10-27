package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PipelinesAndOrgRoleGroupViewTest {

  @Test
  public void testEquals() {
    EqualsVerifier.forClass(PipelinesAndOrgRoleGroupView.class)
        .verify();

  }
}