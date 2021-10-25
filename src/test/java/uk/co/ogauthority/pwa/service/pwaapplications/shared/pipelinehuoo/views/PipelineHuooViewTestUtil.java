package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

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
