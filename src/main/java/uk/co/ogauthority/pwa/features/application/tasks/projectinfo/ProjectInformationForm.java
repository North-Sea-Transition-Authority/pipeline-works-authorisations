package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

public class ProjectInformationForm extends UploadMultipleFilesWithDescriptionForm {


  private String projectName;
  private String projectOverview;
  private String methodOfPipelineDeployment;

  private Integer proposedStartDay;
  private Integer proposedStartMonth;
  private Integer proposedStartYear;

  private Integer mobilisationDay;
  private Integer mobilisationMonth;
  private Integer mobilisationYear;

  private Integer earliestCompletionDay;
  private Integer earliestCompletionMonth;
  private Integer earliestCompletionYear;

  private Integer latestCompletionDay;
  private Integer latestCompletionMonth;
  private Integer latestCompletionYear;

  private Boolean licenceTransferPlanned;

  private String[] pearsApplicationList;
  private String pearsApplicationSelector;

  private Integer licenceTransferDay;
  private Integer licenceTransferMonth;
  private Integer licenceTransferYear;

  private Integer commercialAgreementDay;
  private Integer commercialAgreementMonth;
  private Integer commercialAgreementYear;

  private Boolean usingCampaignApproach;

  private PermanentDepositMade permanentDepositsMadeType;
  private TwoFieldDateInput futureSubmissionDate;

  private Boolean temporaryDepositsMade;
  private String temporaryDepDescription;

  private Boolean fdpOptionSelected;
  private Boolean fdpConfirmationFlag;
  private String fdpNotSelectedReason;


  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getProjectOverview() {
    return projectOverview;
  }

  public void setProjectOverview(String projectOverview) {
    this.projectOverview = projectOverview;
  }

  public String getMethodOfPipelineDeployment() {
    return methodOfPipelineDeployment;
  }

  public void setMethodOfPipelineDeployment(String methodOfPipelineDeployment) {
    this.methodOfPipelineDeployment = methodOfPipelineDeployment;
  }

  public Integer getProposedStartDay() {
    return proposedStartDay;
  }

  public void setProposedStartDay(Integer proposedStartDay) {
    this.proposedStartDay = proposedStartDay;
  }

  public Integer getProposedStartMonth() {
    return proposedStartMonth;
  }

  public void setProposedStartMonth(Integer proposedStartMonth) {
    this.proposedStartMonth = proposedStartMonth;
  }

  public Integer getProposedStartYear() {
    return proposedStartYear;
  }

  public void setProposedStartYear(Integer proposedStartYear) {
    this.proposedStartYear = proposedStartYear;
  }

  public Integer getMobilisationDay() {
    return mobilisationDay;
  }

  public void setMobilisationDay(Integer mobilisationDay) {
    this.mobilisationDay = mobilisationDay;
  }

  public Integer getMobilisationMonth() {
    return mobilisationMonth;
  }

  public void setMobilisationMonth(Integer mobilisationMonth) {
    this.mobilisationMonth = mobilisationMonth;
  }

  public Integer getMobilisationYear() {
    return mobilisationYear;
  }

  public void setMobilisationYear(Integer mobilisationYear) {
    this.mobilisationYear = mobilisationYear;
  }

  public Integer getEarliestCompletionDay() {
    return earliestCompletionDay;
  }

  public void setEarliestCompletionDay(Integer earliestCompletionDay) {
    this.earliestCompletionDay = earliestCompletionDay;
  }

  public Integer getEarliestCompletionMonth() {
    return earliestCompletionMonth;
  }

  public void setEarliestCompletionMonth(Integer earliestCompletionMonth) {
    this.earliestCompletionMonth = earliestCompletionMonth;
  }

  public Integer getEarliestCompletionYear() {
    return earliestCompletionYear;
  }

  public void setEarliestCompletionYear(Integer earliestCompletionYear) {
    this.earliestCompletionYear = earliestCompletionYear;
  }

  public Integer getLatestCompletionDay() {
    return latestCompletionDay;
  }

  public void setLatestCompletionDay(Integer latestCompletionDay) {
    this.latestCompletionDay = latestCompletionDay;
  }

  public Integer getLatestCompletionMonth() {
    return latestCompletionMonth;
  }

  public void setLatestCompletionMonth(Integer latestCompletionMonth) {
    this.latestCompletionMonth = latestCompletionMonth;
  }

  public Integer getLatestCompletionYear() {
    return latestCompletionYear;
  }

  public void setLatestCompletionYear(Integer latestCompletionYear) {
    this.latestCompletionYear = latestCompletionYear;
  }

  public Boolean getUsingCampaignApproach() {
    return usingCampaignApproach;
  }

  public void setUsingCampaignApproach(Boolean usingCampaignApproach) {
    this.usingCampaignApproach = usingCampaignApproach;
  }

  public String[] getPearsApplicationList() {
    return pearsApplicationList;
  }

  public void setPearsApplicationList(String[] pearsApplicationList) {
    this.pearsApplicationList = pearsApplicationList;
  }

  public String getPearsApplicationSelector() {
    return pearsApplicationSelector;
  }

  public void setPearsApplicationSelector(String pearsApplicationSelector) {
    this.pearsApplicationSelector = pearsApplicationSelector;
  }

  public Integer getLicenceTransferDay() {
    return licenceTransferDay;
  }

  public void setLicenceTransferDay(Integer licenceTransferDay) {
    this.licenceTransferDay = licenceTransferDay;
  }

  public Integer getLicenceTransferMonth() {
    return licenceTransferMonth;
  }

  public void setLicenceTransferMonth(Integer licenceTransferMonth) {
    this.licenceTransferMonth = licenceTransferMonth;
  }

  public Integer getLicenceTransferYear() {
    return licenceTransferYear;
  }

  public void setLicenceTransferYear(Integer licenceTransferYear) {
    this.licenceTransferYear = licenceTransferYear;
  }

  public Integer getCommercialAgreementDay() {
    return commercialAgreementDay;
  }

  public void setCommercialAgreementDay(Integer commercialAgreementDay) {
    this.commercialAgreementDay = commercialAgreementDay;
  }

  public Integer getCommercialAgreementMonth() {
    return commercialAgreementMonth;
  }

  public void setCommercialAgreementMonth(Integer commercialAgreementMonth) {
    this.commercialAgreementMonth = commercialAgreementMonth;
  }

  public Integer getCommercialAgreementYear() {
    return commercialAgreementYear;
  }

  public void setCommercialAgreementYear(Integer commercialAgreementYear) {
    this.commercialAgreementYear = commercialAgreementYear;
  }

  public Boolean getLicenceTransferPlanned() {
    return licenceTransferPlanned;
  }

  public void setLicenceTransferPlanned(Boolean licenceTransferPlanned) {
    this.licenceTransferPlanned = licenceTransferPlanned;
  }

  public PermanentDepositMade getPermanentDepositsMadeType() {
    return permanentDepositsMadeType;
  }

  public void setPermanentDepositsMadeType(PermanentDepositMade permanentDepositsMadeType) {
    this.permanentDepositsMadeType = permanentDepositsMadeType;
  }

  public TwoFieldDateInput getFutureSubmissionDate() {
    return futureSubmissionDate;
  }

  public void setFutureSubmissionDate(TwoFieldDateInput futureSubmissionDate) {
    this.futureSubmissionDate = futureSubmissionDate;
  }

  public Boolean getTemporaryDepositsMade() {
    return temporaryDepositsMade;
  }

  public void setTemporaryDepositsMade(Boolean temporaryDepositsMade) {
    this.temporaryDepositsMade = temporaryDepositsMade;
  }

  public String getTemporaryDepDescription() {
    return temporaryDepDescription;
  }

  public void setTemporaryDepDescription(String temporaryDepDescription) {
    this.temporaryDepDescription = temporaryDepDescription;
  }

  public Boolean getFdpOptionSelected() {
    return fdpOptionSelected;
  }

  public void setFdpOptionSelected(Boolean fdpOptionSelected) {
    this.fdpOptionSelected = fdpOptionSelected;
  }

  public Boolean getFdpConfirmationFlag() {
    return fdpConfirmationFlag;
  }

  public void setFdpConfirmationFlag(Boolean fdpConfirmationFlag) {
    this.fdpConfirmationFlag = fdpConfirmationFlag;
  }

  public String getFdpNotSelectedReason() {
    return fdpNotSelectedReason;
  }

  public void setFdpNotSelectedReason(String fdpNotSelectedReason) {
    this.fdpNotSelectedReason = fdpNotSelectedReason;
  }
}
