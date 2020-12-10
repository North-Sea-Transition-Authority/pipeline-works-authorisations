package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary;

import java.util.List;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.Tag;

public class DiffableOrgRolePipelineGroup {

  private final OrganisationRoleOwnerDto roleOwner;
  private final StringWithTag roleOwnerName;
  private final String companyAddress;
  private final String companyNumber;
  private final String treatyAgreementText;
  private final Boolean hasCompanyData;
  private final Boolean isManuallyEnteredName;
  private final List<String> pipelineAndSplitsList;


  public DiffableOrgRolePipelineGroup(OrganisationRoleOwnerDto roleOwner, String roleOwnerName,
                                      String companyAddress,
                                      String companyNumber,
                                      String treatyAgreementText,
                                      Boolean hasCompanyData,
                                      Boolean isManuallyEnteredName,
                                      List<String> pipelineAndSplitsList) {
    this.roleOwner = roleOwner;
    this.roleOwnerName = isManuallyEnteredName ? new StringWithTag(roleOwnerName, Tag.NOT_FROM_PORTAL) : new StringWithTag(roleOwnerName);
    this.companyAddress = companyAddress;
    this.companyNumber = companyNumber;
    this.treatyAgreementText = treatyAgreementText;
    this.hasCompanyData = hasCompanyData;
    this.isManuallyEnteredName = isManuallyEnteredName;
    this.pipelineAndSplitsList = pipelineAndSplitsList;
  }

  public OrganisationRoleOwnerDto getRoleOwner() {
    return roleOwner;
  }

  public StringWithTag getRoleOwnerName() {
    return roleOwnerName;
  }

  public String getCompanyAddress() {
    return companyAddress;
  }

  public String getCompanyNumber() {
    return companyNumber;
  }

  public String getTreatyAgreementText() {
    return treatyAgreementText;
  }

  public Boolean hasCompanyData() {
    return hasCompanyData;
  }

  public Boolean isManuallyEnteredName() {
    return isManuallyEnteredName;
  }

  public List<String> getPipelineAndSplitsList() {
    return pipelineAndSplitsList;
  }
}
