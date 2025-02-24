package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PipelinesAndOrgRoleGroupViewTest {

  @Test
  void equals() {
    EqualsVerifier.forClass(PipelinesAndOrgRoleGroupView.class)
        .verify();

  }
}