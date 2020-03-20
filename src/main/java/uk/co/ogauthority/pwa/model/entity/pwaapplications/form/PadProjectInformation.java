package uk.co.ogauthority.pwa.model.entity.pwaapplications.form;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_project_information")
public class PadProjectInformation {

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
}
