package uk.co.ogauthority.pwa.model.dto.consents;

import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;

public class OrganisationRoleDtoTestUtil {

  public static OrganisationPipelineRoleDto createPipelineRole(HuooRole huooRole, int ouId, int pipelineId) {
    return new OrganisationPipelineRoleDto(
        ouId,
        null,
        huooRole,
        HuooType.PORTAL_ORG,
        pipelineId);
  }

  public static OrganisationRoleDto createOrgRole(HuooRole huooRole, int ouId) {
    return new OrganisationRoleDto(
        ouId,
        null,
        huooRole,
        HuooType.PORTAL_ORG);
  }
}
