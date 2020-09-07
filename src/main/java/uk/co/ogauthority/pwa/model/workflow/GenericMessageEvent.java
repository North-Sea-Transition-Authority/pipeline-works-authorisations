package uk.co.ogauthority.pwa.model.workflow;

import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowMessageEvent;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;

public class GenericMessageEvent implements WorkflowMessageEvent {

  private final WorkflowSubject workflowSubject;
  private final String eventName;

  private GenericMessageEvent(WorkflowSubject workflowSubject, String eventName) {
    this.workflowSubject = workflowSubject;
    this.eventName = eventName;
  }


  public static GenericMessageEvent from(WorkflowSubject workflowSubject, String eventName) {
    return new GenericMessageEvent(
        workflowSubject,
        eventName
    );
  }

  @Override
  public WorkflowSubject getWorkflowSubject() {
    return workflowSubject;
  }

  @Override
  public String getEventName() {
    return eventName;
  }

  @Override
  public String toString() {
    return "GenericMessageEvent{" +
        "workflowSubject=" + workflowSubject.getDebugString() +
        ", eventName='" + eventName + '\'' +
        '}';
  }
}
