package uk.co.ogauthority.pwa.service.enums.workflow;

public enum PwaApplicationWorkflowMessageEvents {

  UPDATE_APPLICATION_REQUEST("updateApplicationRequest");

  private final String messageEventName;

  PwaApplicationWorkflowMessageEvents(String messageEventName) {
    this.messageEventName = messageEventName;
  }


  public String getMessageEventName() {
    return messageEventName;
  }

  public static WorkflowType getWorkFlowType() {
    return WorkflowType.PWA_APPLICATION;
  }

}
