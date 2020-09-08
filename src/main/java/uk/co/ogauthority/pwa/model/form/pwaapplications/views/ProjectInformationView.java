package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import org.apache.commons.lang3.BooleanUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDepositRadioOption;
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

  private final String licenceTransferDate;
  private final String commercialAgreementDate;

  private final Boolean usingCampaignApproach;

  private final Boolean anyDepQuestionRequired;
  private final Boolean permDepQuestionRequired;
  private final PermanentDepositRadioOption permanentDepositsMadeType;
  private final String futureSubmissionDate;

  private final Boolean temporaryDepositsMade;
  private final String temporaryDepDescription;

  private final Boolean fdpQuestionRequired;
  private final Boolean fdpOptionSelected;
  private final Boolean fdpConfirmationFlag;
  private final String fdpNotSelectedReason;

  private final UploadedFileView layoutDiagramFileView;


  public ProjectInformationView(PadProjectInformation padProjectInformation,
                                boolean anyDepQuestionRequired, boolean permDepQuestionRequired, boolean fdpQuestionRequired,
                                UploadedFileView layoutDiagramFileView) {

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

    this.licenceTransferDate = padProjectInformation.getLicenceTransferTimestamp() != null
        ? DateUtils.formatDate(padProjectInformation.getLicenceTransferTimestamp()) : null;

    this.commercialAgreementDate = padProjectInformation.getCommercialAgreementTimestamp() != null
        ? DateUtils.formatDate(padProjectInformation.getCommercialAgreementTimestamp()) : null;

    this.usingCampaignApproach = padProjectInformation.getUsingCampaignApproach();


    this.anyDepQuestionRequired = anyDepQuestionRequired;
    this.permDepQuestionRequired = permDepQuestionRequired;
    if (padProjectInformation.getPermanentDepositsMade() != null) {

      if (BooleanUtils.isFalse(padProjectInformation.getPermanentDepositsMade())) {
        this.permanentDepositsMadeType = PermanentDepositRadioOption.NONE;
        this.futureSubmissionDate = null;

      } else {
        if (padProjectInformation.getFutureAppSubmissionMonth() != null
            && padProjectInformation.getFutureAppSubmissionYear() != null) {
          this.permanentDepositsMadeType = PermanentDepositRadioOption.LATER_APP;
          this.futureSubmissionDate =
              padProjectInformation.getFutureAppSubmissionMonth() + "/" + padProjectInformation.getFutureAppSubmissionYear();
        } else {
          this.permanentDepositsMadeType = PermanentDepositRadioOption.THIS_APP;
          this.futureSubmissionDate = null;
        }

      }
    } else {
      this.permanentDepositsMadeType = null;
      this.futureSubmissionDate = null;
    }

    this.temporaryDepositsMade = padProjectInformation.getTemporaryDepositsMade();
    this.temporaryDepDescription = padProjectInformation.getTemporaryDepDescription();


    this.fdpQuestionRequired = fdpQuestionRequired;
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

  public String getLicenceTransferDate() {
    return licenceTransferDate;
  }

  public String getCommercialAgreementDate() {
    return commercialAgreementDate;
  }

  public Boolean getUsingCampaignApproach() {
    return usingCampaignApproach;
  }

  public Boolean getAnyDepQuestionRequired() {
    return anyDepQuestionRequired;
  }

  public Boolean getPermDepQuestionRequired() {
    return permDepQuestionRequired;
  }

  public PermanentDepositRadioOption getPermanentDepositsMadeType() {
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

  public Boolean getFdpQuestionRequired() {
    return fdpQuestionRequired;
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
