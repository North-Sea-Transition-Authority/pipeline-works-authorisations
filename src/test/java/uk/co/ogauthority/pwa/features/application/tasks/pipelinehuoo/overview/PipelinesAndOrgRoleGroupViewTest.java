package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;

@ExtendWith(MockitoExtension.class)
class PipelinesAndOrgRoleGroupViewTest {

  @Test
  void equals() {
    EqualsVerifier.forClass(PipelinesAndOrgRoleGroupView.class)
        .verify();

  }

  @Test
  void getGroupKey_whenSamePipelinesInDifferentOrder_thenKeysMatch() {
    var pipeline1 = new PipelineId(1);
    var pipeline2 = new PipelineId(2);

    var orderedPipelines = new LinkedHashSet<PipelineIdentifier>(List.of(pipeline1, pipeline2));
    var reversedPipelines = new LinkedHashSet<PipelineIdentifier>(List.of(pipeline2, pipeline1));

    var groupView = PipelineHuooViewTestUtil.createPipelineAndOrgRoleView(
        orderedPipelines,
        Set.of(OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(new OrganisationUnitId(1))),
        List.of("PL1", "PL2"),
        List.of("org 1")
    );
    var otherGroupView = PipelineHuooViewTestUtil.createPipelineAndOrgRoleView(
        reversedPipelines,
        Set.of(OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(new OrganisationUnitId(2))),
        List.of("PL2", "PL1"),
        List.of("org 2")
    );

    assertThat(groupView.getGroupKey()).isEqualTo(otherGroupView.getGroupKey());
  }

  @Test
  void getGroupKey_whenDifferentPipelines_thenKeysDiffer() {
    var groupView = PipelineHuooViewTestUtil.createPipelineAndOrgRoleView(
        Set.of(new PipelineId(1)), Set.of(), List.of("PL1"), List.of());
    var otherGroupView = PipelineHuooViewTestUtil.createPipelineAndOrgRoleView(
        Set.of(new PipelineId(1), new PipelineId(2)), Set.of(), List.of("PL1", "PL2"), List.of());

    assertThat(groupView.getGroupKey()).isNotEqualTo(otherGroupView.getGroupKey());
  }

  @Test
  void getGroupKey_whenManyPipelines_thenKeyLengthIsFixed() {
    Set<PipelineIdentifier> manyPipelines = Set.copyOf(IntStream.rangeClosed(1, 500)
        .mapToObj(PipelineId::new)
        .toList());

    var groupView = PipelineHuooViewTestUtil.createPipelineAndOrgRoleView(
        manyPipelines, Set.of(), List.of(), List.of());

    assertThat(groupView.getGroupKey()).hasSize(64);
  }
}
