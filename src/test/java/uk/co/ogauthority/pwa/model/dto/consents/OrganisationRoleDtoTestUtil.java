package uk.co.ogauthority.pwa.model.dto.consents;

import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
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

  public static OrganisationPipelineRoleInstanceDto createOrgUnitPipelineRoleInstance(HuooRole huooRole, int ouId, int pipelineId) {
    return new OrganisationPipelineRoleInstanceDto(
        ouId,
        null,
        null,
        huooRole,
        HuooType.PORTAL_ORG,
        pipelineId);
  }

  public static OrganisationPipelineRoleInstanceDto createMigratedOrgUnitPipelineRoleInstance(HuooRole huooRole, String orgName, int pipelineId) {
    return new OrganisationPipelineRoleInstanceDto(
        null,
        orgName,
        null,
        huooRole,
        HuooType.PORTAL_ORG,
        pipelineId);
  }

  public static OrganisationPipelineRoleInstanceDto createTreatyOrgUnitPipelineRoleInstance(HuooRole huooRole, TreatyAgreement treatyAgreement, int pipelineId) {
    return new OrganisationPipelineRoleInstanceDto(
        null,
        null,
        treatyAgreement,
        huooRole,
        HuooType.TREATY_AGREEMENT,
        pipelineId);
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
}
