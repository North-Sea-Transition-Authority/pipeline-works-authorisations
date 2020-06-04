package uk.co.ogauthority.pwa.exception;

/**
 * Exception to be used when something has gone wrong trying to assign somebody to a workflow (invalid user, no
 * longer in role etc).
 */
public class WorkflowAssignmentException extends RuntimeException {

  public WorkflowAssignmentException(String message) {
    super(message);
  }

  public WorkflowAssignmentException(String message, Throwable cause) {
    super(message, cause);
  }

}
