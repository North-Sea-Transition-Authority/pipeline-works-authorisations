package uk.co.ogauthority.pwa.service.enums.workflow.publicnotice;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignment;
import uk.co.ogauthority.pwa.integrations.camunda.external.UserWorkflowTask;

public enum PwaApplicationPublicNoticeWorkflowTask implements UserWorkflowTask {

  DRAFT("draft", null),
  MANAGER_APPROVAL("managerApproval", null),
  APPLICANT_UPDATE("applicantUpdate", null),
  CASE_OFFICER_REVIEW("caseOfficerReview", null),
  WAITING("waiting", null),
  PUBLISHED("publish", null);

  private final String taskKey;
  private final WorkflowAssignment workflowAssignment;

  PwaApplicationPublicNoticeWorkflowTask(String taskKey,
                                         WorkflowAssignment workflowAssignment) {
    this.taskKey = taskKey;
    this.workflowAssignment = workflowAssignment;
  }

  @Override
  public WorkflowAssignment getAssignment() {
    return workflowAssignment;
  }

  @Override
  public String getTaskName() {
    return name();
  }

  @Override
  public String getTaskKey() {
    return taskKey;
  }

  public static PwaApplicationPublicNoticeWorkflowTask getByTaskKey(String taskKey) {
    return Stream.of(PwaApplicationPublicNoticeWorkflowTask.values())
        .filter(val -> val.getTaskKey().equals(taskKey))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(
            String.format("Couldn't get PwaApplicationPublicNoticeWorkflowTask from taskKey: %s", taskKey)));
  }

}
