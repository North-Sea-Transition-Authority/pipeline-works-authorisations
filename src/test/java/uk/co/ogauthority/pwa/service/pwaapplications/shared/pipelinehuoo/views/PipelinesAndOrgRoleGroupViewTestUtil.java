package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.List;
import java.util.Set;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;

public class PipelinesAndOrgRoleGroupViewTestUtil {

  private PipelinesAndOrgRoleGroupViewTestUtil() {
    // no instantiation
  }

  public static PipelinesAndOrgRoleGroupView createSinglePipelineSingleOrgGroupView(
      PipelineIdentifier pipelineIdentifier,
      OrganisationRoleOwnerDto organisationRoleOwnerDto,
      String pipelineName,
      String orgName
  ) {
    return new PipelinesAndOrgRoleGroupView(Set.of(pipelineIdentifier), Set.of(organisationRoleOwnerDto),
        List.of(pipelineName), List.of(orgName));
  }

  public static PipelinesAndOrgRoleGroupView createMultiPipelineSingleOrgGroupView(
      Set<PipelineIdentifier> pipelineIdentifiers,
      List<String> pipelineNames,
      OrganisationRoleOwnerDto organisationRoleOwnerDto,
      String orgName
  ) {
    return new PipelinesAndOrgRoleGroupView(pipelineIdentifiers, Set.of(organisationRoleOwnerDto),
        pipelineNames, List.of(orgName));
  }
}