package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

public class PadOrganisationRoleTestUtil {

  public static PadOrganisationRole createOrgRole(HuooRole role) {
    var organisationRole = new PadOrganisationRole();
    organisationRole.setRole(role);
    return organisationRole;
  }

  public static PadOrganisationRole createOrgRole(HuooRole role, PortalOrganisationUnit portalOrganisationUnit) {
    var orgRole = createOrgRole(role);
    orgRole.setOrganisationUnit(portalOrganisationUnit);
    return orgRole;
  }

  public static PadPipelineOrganisationRoleLink createOrgRolePipelineLink(HuooRole role,
                                                                          PortalOrganisationUnit portalOrganisationUnit,
                                                                          Pipeline pipeline) {
    var orgRole = createOrgRole(role);
    return new PadPipelineOrganisationRoleLink(orgRole, pipeline);
  }

}
