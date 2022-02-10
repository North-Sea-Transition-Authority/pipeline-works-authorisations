package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;

public class PipelineHuooViewTestUtil {

  public static PipelinesAndOrgRoleGroupView createPipelineAndOrgRoleView(Set<PipelineIdentifier> pipipelineIdentifiers,
                                                                          Set<OrganisationRoleOwnerDto> organisationRoleOwnerDtos,
                                                                          List<String> pipelineNames,
                                                                          List<String> orgRoleNames
  ) {
    return new PipelinesAndOrgRoleGroupView(
        pipipelineIdentifiers,
        organisationRoleOwnerDtos,
        pipelineNames,
        orgRoleNames
    );
  }

  public static PipelineHuooRoleSummaryView createUnassignedPipelinePipelineHuooRoleSummaryView(HuooRole huooRole,
                                                                                                Set<PipelineId> unassignedPipelines
  ) {

    return new PipelineHuooRoleSummaryView(huooRole,
        Collections.emptyList(),
        unassignedPipelines.stream().collect(Collectors.toMap(o -> o, o -> "Something")),
        Collections.emptyMap());

  }


}
