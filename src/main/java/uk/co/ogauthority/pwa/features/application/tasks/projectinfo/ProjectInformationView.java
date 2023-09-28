package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.util.List;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.util.DateUtils;

public class ProjectInformationView {


  private final String projectName;

  private final String projectOverview;

  private final String methodOfPipelineDeployment;

  private final String proposedStartDate;
  private final String mobilisationDate;
  private final String earliestCompletionDate;
  private final String latestCompletionDate;

  private final Boolean licenceTransferPlanned;

  private final List<String> licenceReferences;
  private final String licenceTransferDate;
  private final String commercialAgreementDate;

  private final Boolean usingCampaignApproach;

  private final PermanentDepositMade permanentDepositsMadeType;
  private final String futureSubmissionDate;

  private final Boolean temporaryDepositsMade;
  private final String temporaryDepDescription;

  private final Boolean isFdpQuestionRequiredBasedOnField;
  private final Boolean fdpOptionSelected;
  private final Boolean fdpConfirmationFlag;
  private final String fdpNotSelectedReason;

  private final UploadedFileView layoutDiagramFileView;


  public ProjectInformationView(PadProjectInformation padProjectInformation,
                                boolean isFdpQuestionRequiredBasedOnField,
                                UploadedFileView layoutDiagramFileView,
                                List<String> licenceApplications) {

    this.projectName = padProjectInformation.getProjectName();
    this.projectOverview = padProjectInformation.getProjectOverview();
    this.methodOfPipelineDeployment = padProjectInformation.getMethodOfPipelineDeployment();

    this.proposedStartDate = padProjectInformation.getProposedStartTimestamp() != null
        ? DateUtils.formatDate(padProjectInformation.getProposedStartTimestamp()) : null;

    this.mobilisationDate = padProjectInformation.getMobilisationTimestamp() != null
        ? DateUtils.formatDate(padProjectInformation.getMobilisationTimestamp()) : null;

    this.earliestCompletionDate = padProjectInformation.getEarliestCompletionTimestamp() != null
        ? DateUtils.formatDate(padProjectInformation.getEarliestCompletionTimestamp()) : null;

    this.latestCompletionDate = padProjectInformation.getLatestCompletionTimestamp() != null
        ? DateUtils.formatDate(padProjectInformation.getLatestCompletionTimestamp()) : null;

    this.licenceTransferPlanned = padProjectInformation.getLicenceTransferPlanned();

    this.licenceReferences = licenceApplications;
    this.licenceTransferDate = padProjectInformation.getLicenceTransferTimestamp() != null
        ? DateUtils.formatDate(padProjectInformation.getLicenceTransferTimestamp()) : null;

    this.commercialAgreementDate = padProjectInformation.getCommercialAgreementTimestamp() != null
        ? DateUtils.formatDate(padProjectInformation.getCommercialAgreementTimestamp()) : null;

    this.usingCampaignApproach = padProjectInformation.getUsingCampaignApproach();


    if (padProjectInformation.getPermanentDepositsMade() != null) {
      this.permanentDepositsMadeType = padProjectInformation.getPermanentDepositsMade();

      if (padProjectInformation.getFutureAppSubmissionMonth() != null
          && padProjectInformation.getFutureAppSubmissionYear() != null) {
        this.futureSubmissionDate =
            padProjectInformation.getFutureAppSubmissionMonth() + "/" + padProjectInformation.getFutureAppSubmissionYear();
      } else {
        this.futureSubmissionDate = null;
      }
    } else {
      this.permanentDepositsMadeType = null;
      this.futureSubmissionDate = null;
    }

    this.temporaryDepositsMade = padProjectInformation.getTemporaryDepositsMade();
    this.temporaryDepDescription = padProjectInformation.getTemporaryDepDescription();

    this.isFdpQuestionRequiredBasedOnField = isFdpQuestionRequiredBasedOnField;
    this.fdpOptionSelected = padProjectInformation.getFdpOptionSelected();
    this.fdpConfirmationFlag = padProjectInformation.getFdpConfirmationFlag();
    this.fdpNotSelectedReason = padProjectInformation.getFdpNotSelectedReason();

    this.layoutDiagramFileView = layoutDiagramFileView;
  }


  public String getProjectName() {
    return projectName;
  }

  public String getProjectOverview() {
    return projectOverview;
  }

  public String getMethodOfPipelineDeployment() {
    return methodOfPipelineDeployment;
  }

  public String getProposedStartDate() {
    return proposedStartDate;
  }

  public String getMobilisationDate() {
    return mobilisationDate;
  }

  public String getEarliestCompletionDate() {
    return earliestCompletionDate;
  }

  public String getLatestCompletionDate() {
    return latestCompletionDate;
  }

  public Boolean getLicenceTransferPlanned() {
    return licenceTransferPlanned;
  }

  public List<String> getLicenceReferences() {
    return licenceReferences;
  }

  public String getLicenceTransferDate() {
    return licenceTransferDate;
  }

  public String getCommercialAgreementDate() {
    return commercialAgreementDate;
  }

  public Boolean getUsingCampaignApproach() {
    return usingCampaignApproach;
  }

  public PermanentDepositMade getPermanentDepositsMadeType() {
    return permanentDepositsMadeType;
  }

  public String getFutureSubmissionDate() {
    return futureSubmissionDate;
  }

  public Boolean getTemporaryDepositsMade() {
    return temporaryDepositsMade;
  }

  public String getTemporaryDepDescription() {
    return temporaryDepDescription;
  }

  public Boolean getIsFdpQuestionRequiredBasedOnField() {
    return isFdpQuestionRequiredBasedOnField;
  }

  public Boolean getFdpOptionSelected() {
    return fdpOptionSelected;
  }

  public Boolean getFdpConfirmationFlag() {
    return fdpConfirmationFlag;
  }

  public String getFdpNotSelectedReason() {
    return fdpNotSelectedReason;
  }

  public UploadedFileView getLayoutDiagramFileView() {
    return layoutDiagramFileView;
  }
}
