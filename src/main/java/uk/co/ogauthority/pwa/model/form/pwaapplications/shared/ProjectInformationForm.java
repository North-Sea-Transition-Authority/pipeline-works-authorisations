package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

public class ProjectInformationForm extends UploadMultipleFilesWithDescriptionForm {


  @NotNull(message = "Enter the project name", groups = {FullValidation.class})
  @Length(max = 4000, message = "Project name must be 4000 characters or fewer",
      groups = {FullValidation.class, PartialValidation.class})
  private String projectName;

  @NotNull(message = "Enter the project overview", groups = {FullValidation.class})
  @Length(max = 4000, message = "Project overview must be 4000 characters or fewer",
      groups = {FullValidation.class, PartialValidation.class})
  private String projectOverview;

  @NotNull(message = "Enter the pipeline installation method", groups = {FullValidation.class})
  @Length(max = 4000, message = "Pipeline installation method must be 4000 characters or fewer",
      groups = {FullValidation.class, PartialValidation.class})
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

  @NotNull(message = "Select yes if a licence transfer is planned", groups = {FullValidation.class})
  private Boolean licenceTransferPlanned;

  private Integer licenceTransferDay;
  private Integer licenceTransferMonth;
  private Integer licenceTransferYear;

  private Integer commercialAgreementDay;
  private Integer commercialAgreementMonth;
  private Integer commercialAgreementYear;

  @NotNull(message = "Select yes if using a campaign approach", groups = {FullValidation.class})
  private Boolean usingCampaignApproach;

  private Boolean isPermanentDepositsMade;
  private Integer futureAppSubmissionMonth;
  private Integer futureAppSubmissionYear;

  private Boolean isTemporaryDepositsMade;
  private String temporaryDepDescription;


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


  public Boolean getIsPermanentDepositsMade() {
    return isPermanentDepositsMade;
  }

  public void setIsPermanentDepositsMade(Boolean permanentDepositsMade) {
    isPermanentDepositsMade = permanentDepositsMade;
  }

  public Integer getFutureAppSubmissionMonth() {
    return futureAppSubmissionMonth;
  }

  public void setFutureAppSubmissionMonth(Integer futureAppSubmissionMonth) {
    this.futureAppSubmissionMonth = futureAppSubmissionMonth;
  }

  public Integer getFutureAppSubmissionYear() {
    return futureAppSubmissionYear;
  }

  public void setFutureAppSubmissionYear(Integer futureAppSubmissionYear) {
    this.futureAppSubmissionYear = futureAppSubmissionYear;
  }

  public Boolean getIsTemporaryDepositsMade() {
    return isTemporaryDepositsMade;
  }

  public void setIsTemporaryDepositsMade(Boolean temporaryDepositsMade) {
    isTemporaryDepositsMade = temporaryDepositsMade;
  }

  public String getTemporaryDepDescription() {
    return temporaryDepDescription;
  }

  public void setTemporaryDepDescription(String temporaryDepDescription) {
    this.temporaryDepDescription = temporaryDepDescription;
  }
}
