package uk.co.ogauthority.pwa.model.entity.enums.publicnotice;

import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;

public enum PublicNoticeStatus {

  DRAFT("Draft", PwaApplicationPublicNoticeWorkflowTask.DRAFT),
  MANAGER_APPROVAL("Manager approval", PwaApplicationPublicNoticeWorkflowTask.MANAGER_APPROVAL),
  APPLICANT_UPDATE("Applicant update", PwaApplicationPublicNoticeWorkflowTask.APPLICANT_UPDATE),
  CASE_OFFICER_REVIEW("Case officer review", PwaApplicationPublicNoticeWorkflowTask.CASE_OFFICER_REVIEW),
  WAITING("Waiting", PwaApplicationPublicNoticeWorkflowTask.WAITING),
  WITHDRAWN("Withdrawn", null),
  PUBLISHED("Published", PwaApplicationPublicNoticeWorkflowTask.PUBLISHED),
  ENDED("Ended", null);

  private final String displayText;
  private final PwaApplicationPublicNoticeWorkflowTask workflowTask;

  PublicNoticeStatus(String displayText,
                     PwaApplicationPublicNoticeWorkflowTask workflowTask) {
    this.displayText = displayText;
    this.workflowTask = workflowTask;
  }


  public String getDisplayText() {
    return displayText;
  }

  public PwaApplicationPublicNoticeWorkflowTask getWorkflowTask() {
    return workflowTask;
  }
}
