package uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments;

import uk.co.ogauthority.pwa.service.enums.users.UserType;

public enum WorkflowAssignment {

  CASE_OFFICER(UserType.OGA),

  CONSULTATION_RESPONDER(UserType.CONSULTEE);

  private final UserType userType;

  WorkflowAssignment(UserType userType) {
    this.userType = userType;
  }

  public UserType getUserType() {
    return userType;
  }

}
