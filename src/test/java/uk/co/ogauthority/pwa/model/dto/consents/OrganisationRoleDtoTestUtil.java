package uk.co.ogauthority.pwa.model.dto.consents;

import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;

public class OrganisationRoleDtoTestUtil {

  public static OrganisationPipelineRoleInstanceDto createOrgUnitPipelineRoleInstance(HuooRole huooRole, int ouId, int pipelineId) {
    return new OrganisationPipelineRoleInstanceDto(
        ouId,
        null,
        huooRole,
        HuooType.PORTAL_ORG,
        pipelineId);
  }

  public static OrganisationRoleInstanceDto createOrganisationUnitOrgRoleInstance(HuooRole huooRole, int ouId) {
    return new OrganisationRoleInstanceDto(
        ouId,
        null,
        huooRole,
        HuooType.PORTAL_ORG);
  }
}
