package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary;

import java.util.List;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

public class OrganisationRolePipelineGroupView {

  private final HuooType huooType;
  private final String companyName;
  private final TreatyAgreement treatyAgreement;
  private final OrganisationRoleOwnerDto organisationRoleOwner;
  private final String registeredNumber;
  private final String companyAddress;
  private final List<PipelineNumbersAndSplits> pipelineNumbersAndSplits;


  public OrganisationRolePipelineGroupView(HuooType huooType,
                                           String companyName,
                                           TreatyAgreement treatyAgreement,
                                           OrganisationRoleOwnerDto organisationRoleOwner,
                                           String registeredNumber, String companyAddress,
                                           List<PipelineNumbersAndSplits> pipelineNumbersAndSplits) {
    this.huooType = huooType;
    this.companyName = companyName;
    this.treatyAgreement = treatyAgreement;
    this.organisationRoleOwner = organisationRoleOwner;
    this.registeredNumber = registeredNumber;
    this.companyAddress = companyAddress;
    this.pipelineNumbersAndSplits = pipelineNumbersAndSplits;
  }

  public HuooType getHuooType() {
    return huooType;
  }

  public String getCompanyName() {
    return companyName;
  }

  public TreatyAgreement getTreatyAgreement() {
    return treatyAgreement;
  }

  public OrganisationRoleOwnerDto getOrganisationRoleOwner() {
    return organisationRoleOwner;
  }

  public String getRegisteredNumber() {
    return registeredNumber;
  }

  public String getCompanyAddress() {
    return companyAddress;
  }

  public List<PipelineNumbersAndSplits> getPipelineNumbersAndSplits() {
    return pipelineNumbersAndSplits;
  }

}
