package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

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