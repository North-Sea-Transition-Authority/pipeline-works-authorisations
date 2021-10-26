package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;

public class OrganisationRolePipelineGroupView {

  private final HuooType huooType;
  private final OrganisationUnitDetailDto orgUnitDetailDto;
  private final Boolean isManuallyEnteredName;
  private final String manuallyEnteredName;
  private final TreatyAgreement treatyAgreement;
  private final OrganisationRoleOwnerDto organisationRoleOwner;
  private final List<PipelineNumbersAndSplits> pipelineNumbersAndSplits;


  public OrganisationRolePipelineGroupView(HuooType huooType,
                                           OrganisationUnitDetailDto orgUnitDetailDto,
                                           Boolean isManuallyEnteredName,
                                           String manuallyEnteredName,
                                           TreatyAgreement treatyAgreement,
                                           OrganisationRoleOwnerDto organisationRoleOwner,
                                           List<PipelineNumbersAndSplits> pipelineNumbersAndSplits) {
    this.huooType = huooType;
    this.orgUnitDetailDto = orgUnitDetailDto;
    this.isManuallyEnteredName = isManuallyEnteredName;
    this.manuallyEnteredName = manuallyEnteredName;
    this.treatyAgreement = treatyAgreement;
    this.organisationRoleOwner = organisationRoleOwner;
    this.pipelineNumbersAndSplits = pipelineNumbersAndSplits.stream()
        .sorted(Comparator.comparing(PipelineNumbersAndSplits::toString)).collect(
        Collectors.toList());
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

  public Set<PipelineIdentifier> getPipelineIdentifiersInGroup() {
    return this.pipelineNumbersAndSplits.stream()
        .map(PipelineNumbersAndSplits::getPipelineIdentifier)
        .collect(Collectors.toSet());
  }

}
