package uk.co.ogauthority.pwa.model.workflow;

import java.util.Objects;
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GenericMessageEvent that = (GenericMessageEvent) o;
    return Objects.equals(workflowSubject, that.workflowSubject)
        && Objects.equals(eventName, that.eventName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(workflowSubject, eventName);
  }

  @Override
  public String toString() {
    return "GenericMessageEvent{" +
        "workflowSubject=" + workflowSubject.getDebugString() +
        ", eventName='" + eventName + '\'' +
        '}';
  }
}
