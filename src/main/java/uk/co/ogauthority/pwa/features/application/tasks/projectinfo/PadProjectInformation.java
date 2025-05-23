package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity(name = "pad_project_information")
public class PadProjectInformation implements ChildEntity<Integer, PwaApplicationDetail> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_detail_id")
  @OneToOne
  private PwaApplicationDetail pwaApplicationDetail;

  private String projectName;
  private String projectOverview;

  // TODO: PWA-381 Map to entity from Files table.
  private Integer projectDiagramFileId;
  private String methodOfPipelineDeployment;
  private Instant proposedStartTimestamp;
  private Instant mobilisationTimestamp;
  private Instant earliestCompletionTimestamp;
  private Instant latestCompletionTimestamp;
  private Boolean usingCampaignApproach;

  private Boolean licenceTransferPlanned;
  private Instant licenceTransferTimestamp;
  private Instant commercialAgreementTimestamp;

  @Enumerated(EnumType.STRING)
  private PermanentDepositMade permanentDepositsMade;
  private Integer futureAppSubmissionMonth;
  private Integer futureAppSubmissionYear;

  private Boolean temporaryDepositsMade;
  private String temporaryDepDescription;

  private Boolean fdpOptionSelected;
  private Boolean fdpConfirmationFlag;
  private String fdpNotSelectedReason;

  private Boolean cspOptionSelected;
  private Boolean cspConfirmationFlag;
  private String cspNotSelectedReason;


  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PwaApplicationDetail parentEntity) {
    this.pwaApplicationDetail = parentEntity;
  }

  @Override
  public PwaApplicationDetail getParent() {
    return this.pwaApplicationDetail;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

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

  public Integer getProjectDiagramFileId() {
    return projectDiagramFileId;
  }

  public void setProjectDiagramFileId(Integer projectDiagramFileId) {
    this.projectDiagramFileId = projectDiagramFileId;
  }

  public String getMethodOfPipelineDeployment() {
    return methodOfPipelineDeployment;
  }

  public void setMethodOfPipelineDeployment(String methodOfPipelineDeployment) {
    this.methodOfPipelineDeployment = methodOfPipelineDeployment;
  }

  public Instant getProposedStartTimestamp() {
    return proposedStartTimestamp;
  }

  public void setProposedStartTimestamp(Instant proposedStartTimestamp) {
    this.proposedStartTimestamp = proposedStartTimestamp;
  }

  public Instant getMobilisationTimestamp() {
    return mobilisationTimestamp;
  }

  public void setMobilisationTimestamp(Instant mobilisationTimestamp) {
    this.mobilisationTimestamp = mobilisationTimestamp;
  }

  public Instant getEarliestCompletionTimestamp() {
    return earliestCompletionTimestamp;
  }

  public void setEarliestCompletionTimestamp(Instant earliestCompletionTimestamp) {
    this.earliestCompletionTimestamp = earliestCompletionTimestamp;
  }

  public Instant getLatestCompletionTimestamp() {
    return latestCompletionTimestamp;
  }

  public void setLatestCompletionTimestamp(Instant latestCompletionTimestamp) {
    this.latestCompletionTimestamp = latestCompletionTimestamp;
  }

  public Boolean getUsingCampaignApproach() {
    return usingCampaignApproach;
  }

  public void setUsingCampaignApproach(Boolean usingCampaignApproach) {
    this.usingCampaignApproach = usingCampaignApproach;
  }

  public Boolean getLicenceTransferPlanned() {
    return licenceTransferPlanned;
  }

  public void setLicenceTransferPlanned(Boolean licenceTransferPlanned) {
    this.licenceTransferPlanned = licenceTransferPlanned;
  }

  public Instant getLicenceTransferTimestamp() {
    return licenceTransferTimestamp;
  }

  public void setLicenceTransferTimestamp(Instant licenceTransferTimestamp) {
    this.licenceTransferTimestamp = licenceTransferTimestamp;
  }

  public Instant getCommercialAgreementTimestamp() {
    return commercialAgreementTimestamp;
  }

  public void setCommercialAgreementTimestamp(Instant commercialAgreementTimestamp) {
    this.commercialAgreementTimestamp = commercialAgreementTimestamp;
  }

  public PermanentDepositMade getPermanentDepositsMade() {
    return permanentDepositsMade;
  }

  public void setPermanentDepositsMade(PermanentDepositMade permanentDepositsMade) {
    this.permanentDepositsMade = permanentDepositsMade;
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

  public Boolean getCspOptionSelected() {
    return cspOptionSelected;
  }

  public PadProjectInformation setCspOptionSelected(Boolean cspOptionSelected) {
    this.cspOptionSelected = cspOptionSelected;
    return this;
  }

  public Boolean getCspConfirmationFlag() {
    return cspConfirmationFlag;
  }

  public PadProjectInformation setCspConfirmationFlag(Boolean cspConfirmationFlag) {
    this.cspConfirmationFlag = cspConfirmationFlag;
    return this;
  }

  public String getCspNotSelectedReason() {
    return cspNotSelectedReason;
  }

  public PadProjectInformation setCspNotSelectedReason(String cspNotSelectedReason) {
    this.cspNotSelectedReason = cspNotSelectedReason;
    return this;
  }
}
