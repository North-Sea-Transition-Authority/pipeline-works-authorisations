package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary;

import java.util.List;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

public class OrganisationRolePipelineGroupView {

  private final HuooType huooType;
  private final OrganisationUnitDetailDto orgUnitDetailDto;
  private final Boolean isManuallyEnteredName;
  private final String manuallyEnteredName;
  private final TreatyAgreement treatyAgreement;
  private final OrganisationRoleOwnerDto organisationRoleOwner;
  private final List<PipelineNumbersAndSplits> pipelineNumbersAndSplits;


  public OrganisationRolePipelineGroupView(HuooType huooType,
                                           OrganisationUnitDetailDto orgUnitDetailDto, Boolean isManuallyEnteredName,
                                           String manuallyEnteredName, TreatyAgreement treatyAgreement,
                                           OrganisationRoleOwnerDto organisationRoleOwner,
                                           List<PipelineNumbersAndSplits> pipelineNumbersAndSplits) {
    this.huooType = huooType;
    this.orgUnitDetailDto = orgUnitDetailDto;
    this.isManuallyEnteredName = isManuallyEnteredName;
    this.manuallyEnteredName = manuallyEnteredName;
    this.treatyAgreement = treatyAgreement;
    this.organisationRoleOwner = organisationRoleOwner;
    this.pipelineNumbersAndSplits = pipelineNumbersAndSplits;
  }

  public HuooType getHuooType() {
    return huooType;
  }

  public OrganisationUnitDetailDto getOrgUnitDetailDto() {
    return orgUnitDetailDto;
  }

  public Boolean getIsManuallyEnteredName() {
    return isManuallyEnteredName;
  }

  public String getManuallyEnteredName() {
    return manuallyEnteredName;
  }

  public TreatyAgreement getTreatyAgreement() {
    return treatyAgreement;
  }

  public OrganisationRoleOwnerDto getOrganisationRoleOwner() {
    return organisationRoleOwner;
  }

  public List<PipelineNumbersAndSplits> getPipelineNumbersAndSplits() {
    return pipelineNumbersAndSplits;
  }

  public String getCompanyName() {
    return orgUnitDetailDto != null ? orgUnitDetailDto.getCompanyName() : null;
  }

  public String getCompanyAddress() {
    return orgUnitDetailDto != null ? orgUnitDetailDto.getCompanyAddress() : null;
  }

  public String getRegisteredNumber() {
    return orgUnitDetailDto != null ? orgUnitDetailDto.getRegisteredNumber() : null;
  }

}
