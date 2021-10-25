package uk.co.ogauthority.pwa.model.dto.consents;

import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

public class OrganisationRoleDtoTestUtil {

  public static OrganisationRoleOwnerDto createOrganisationUnitRoleOwnerDto(int orgUnitId){
    return createOrganisationUnitRoleOwnerDto(new OrganisationUnitId(orgUnitId));
  }

  public static OrganisationRoleOwnerDto createOrganisationUnitRoleOwnerDto(OrganisationUnitId orgUnitId){
    return new OrganisationRoleOwnerDto(
        HuooType.PORTAL_ORG,
        orgUnitId,
        null,
        null
    );
  }

  public static OrganisationRoleOwnerDto createTreatyRoleOwnerDto(TreatyAgreement treatyAgreement){
    return new OrganisationRoleOwnerDto(
        HuooType.TREATY_AGREEMENT,
        null,
        null,
        treatyAgreement
    );
  }

  public static OrganisationPipelineRoleInstanceDto createOrgUnitPipelineRoleInstance(HuooRole huooRole, int ouId, Integer pipelineId) {
    return new OrganisationPipelineRoleInstanceDto(
        ouId,
        null,
        null,
        huooRole,
        HuooType.PORTAL_ORG,
        pipelineId,
        null,
        null,
        null,
        null,
        null
    );
  }

  public static OrganisationPipelineRoleInstanceDto createOrgUnitPipelineSectionRoleInstance(HuooRole huooRole,
                                                                                             int ouId,
                                                                                             int pipelineId,
                                                                                             String from,
                                                                                             String to,
                                                                                             int sectionNumber) {
    return new OrganisationPipelineRoleInstanceDto(
        ouId,
        null,
        null,
        huooRole,
        HuooType.PORTAL_ORG,
        pipelineId,
        from,
        IdentLocationInclusionMode.INCLUSIVE,
        to,
        IdentLocationInclusionMode.EXCLUSIVE,
        sectionNumber
    );
  }

  public static OrganisationPipelineRoleInstanceDto createOrgUnitPipelineSectionRoleInstance(HuooRole huooRole,
                                                                                             int ouId,
                                                                                             PipelineSection pipelineSection) {
    return new OrganisationPipelineRoleInstanceDto(
        ouId,
        null,
        null,
        huooRole,
        HuooType.PORTAL_ORG,
        pipelineSection.getPipelineIdAsInt(),
        pipelineSection.getFromPoint().getLocationName(),
        pipelineSection.getFromPoint().getIdentLocationInclusionMode(),
        pipelineSection.getToPoint().getLocationName(),
        pipelineSection.getToPoint().getIdentLocationInclusionMode(),
        pipelineSection.getSectionNumber()
    );
  }

  public static OrganisationPipelineRoleInstanceDto createUnassignedSplitPipelineSectionRoleInstance(HuooRole huooRole,
                                                                                                     PipelineSection pipelineSection) {
    return new OrganisationPipelineRoleInstanceDto(
        null,
        null,
        null,
        huooRole,
        HuooType.UNASSIGNED_PIPELINE_SPLIT,
        pipelineSection.getPipelineIdAsInt(),
        pipelineSection.getFromPoint().getLocationName(),
        pipelineSection.getFromPoint().getIdentLocationInclusionMode(),
        pipelineSection.getToPoint().getLocationName(),
        pipelineSection.getToPoint().getIdentLocationInclusionMode(),
        pipelineSection.getSectionNumber()
    );
  }

  public static OrganisationPipelineRoleInstanceDto createMigratedOrgUnitPipelineRoleInstance(HuooRole huooRole, String orgName, int pipelineId) {
    return new OrganisationPipelineRoleInstanceDto(
        null,
        orgName,
        null,
        huooRole,
        HuooType.PORTAL_ORG,
        pipelineId,
        null,
        null,
        null,
        null,
        null

    );
  }

  public static OrganisationPipelineRoleInstanceDto createTreatyOrgUnitPipelineRoleInstance(HuooRole huooRole, TreatyAgreement treatyAgreement, int pipelineId) {
    return new OrganisationPipelineRoleInstanceDto(
        null,
        null,
        treatyAgreement,
        huooRole,
        HuooType.TREATY_AGREEMENT,
        pipelineId,
        null,
        null,
        null,
        null,
        null
    );
  }

  public static OrganisationRoleInstanceDto createOrganisationUnitOrgRoleInstance(HuooRole huooRole, int ouId) {
    return new OrganisationRoleInstanceDto(
        ouId,
        null,
        null,
        huooRole,
        HuooType.PORTAL_ORG);
  }

  public static OrganisationRoleInstanceDto createMigratedOrgRoleInstance(HuooRole huooRole, String name) {
    return new OrganisationRoleInstanceDto(
        null,
        name,
        null,
        huooRole,
        HuooType.PORTAL_ORG);
  }

  public static OrganisationRoleInstanceDto createTreatyOrgRoleInstance(HuooRole huooRole, TreatyAgreement treatyAgreement) {
    return new OrganisationRoleInstanceDto(
        null,
        null,
        treatyAgreement,
        huooRole,
        HuooType.TREATY_AGREEMENT);
  }

  public static OrganisationRoleInstanceDto createUnassignedPipelineSectionRoleInstance(HuooRole huooRole){
    return new OrganisationRoleInstanceDto(
        null,
        null,
        null,
        huooRole,
        HuooType.UNASSIGNED_PIPELINE_SPLIT);
  }
}
