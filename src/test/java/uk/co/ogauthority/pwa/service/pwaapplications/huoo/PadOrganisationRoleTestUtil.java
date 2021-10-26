package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

public class PadOrganisationRoleTestUtil {

  public static PadOrganisationRole createOrgRole(HuooRole role) {
    var organisationRole = new PadOrganisationRole();
    organisationRole.setRole(role);
    organisationRole.setType(HuooType.PORTAL_ORG);
    return organisationRole;
  }

  public static PadOrganisationRole createOrgRole(HuooRole role, PortalOrganisationUnit portalOrganisationUnit) {
    var orgRole = createOrgRole(role);
    orgRole.setOrganisationUnit(portalOrganisationUnit);
    orgRole.setType(HuooType.PORTAL_ORG);
    return orgRole;
  }

  public static PadOrganisationRole createTreatyRole(HuooRole role, TreatyAgreement treatyAgreement) {
    var orgRole = createOrgRole(role);
    orgRole.setAgreement(treatyAgreement);
    orgRole.setType(HuooType.TREATY_AGREEMENT);
    return orgRole;
  }

  public static PadPipelineOrganisationRoleLink createOrgRolePipelineLink(HuooRole role,
                                                                          PortalOrganisationUnit portalOrganisationUnit,
                                                                          Pipeline pipeline) {
    var orgRole = createOrgRole(role, portalOrganisationUnit);
    return new PadPipelineOrganisationRoleLink(orgRole, pipeline);
  }

  public static PadPipelineOrganisationRoleLink createOrgRoleInclusivePipelineSplitLink(HuooRole role,
                                                                                        PortalOrganisationUnit portalOrganisationUnit,
                                                                                        Pipeline pipeline,
                                                                                        String fromLocation,
                                                                                        String toLocation,
                                                                                        int sectionNumber) {
    var orgRole = createOrgRole(role, portalOrganisationUnit);
    var link = new PadPipelineOrganisationRoleLink(orgRole, pipeline);
    link.setFromLocation(fromLocation);
    link.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    link.setToLocation(toLocation);
    link.setToLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    link.setSectionNumber(sectionNumber);
    return link;
  }

  public static PadPipelineOrganisationRoleLink createOrgRolePipelineSplitLink(HuooRole role,
                                                                               PortalOrganisationUnit portalOrganisationUnit,
                                                                               PipelineSection pipelineSection) {
    var orgRole = createOrgRole(role, portalOrganisationUnit);
    var pipeline = new Pipeline();
    pipeline.setId(pipelineSection.getPipelineIdAsInt());
    var link = new PadPipelineOrganisationRoleLink(orgRole, pipeline);
    link.visit(pipelineSection);
    return link;
  }

  public static PadPipelineOrganisationRoleLink createUnassignedPipelineSplitLink(HuooRole role,
                                                                                  PipelineSection pipelineSection) {
    var orgRole = createOrgRole(role);
    orgRole.setType(HuooType.UNASSIGNED_PIPELINE_SPLIT);
    var pipeline = new Pipeline();
    pipeline.setId(pipelineSection.getPipelineIdAsInt());
    var link = new PadPipelineOrganisationRoleLink(orgRole, pipeline);
    link.visit(pipelineSection);
    return link;
  }

  public static PadPipelineOrganisationRoleLink createOrgRoleInclusivePipelineSplitLink(
      PadOrganisationRole padOrganisationRole,
      Pipeline pipeline,
      String fromLocation,
      String toLocation,
      int sectionNumber) {

    var link = new PadPipelineOrganisationRoleLink(padOrganisationRole, pipeline);
    link.setFromLocation(fromLocation);
    link.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    link.setToLocation(toLocation);
    link.setToLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    link.setSectionNumber(sectionNumber);
    return link;
  }

}
