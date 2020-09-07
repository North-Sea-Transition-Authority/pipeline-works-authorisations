package uk.co.ogauthority.pwa.model.workflow;

import java.util.Objects;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;

public class WorkflowBusinessKey {
  private final String value;

  private WorkflowBusinessKey(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static WorkflowBusinessKey from(Integer integer) {
    return new WorkflowBusinessKey(String.valueOf(integer));
  }

  public static WorkflowBusinessKey from(String businessKey) {
    return new WorkflowBusinessKey(businessKey);
  }

  public static WorkflowBusinessKey from(WorkflowSubject workflowSubject) {
    return WorkflowBusinessKey.from(workflowSubject.getBusinessKey());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkflowBusinessKey that = (WorkflowBusinessKey) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
