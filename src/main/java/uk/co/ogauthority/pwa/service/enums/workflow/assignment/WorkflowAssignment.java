package uk.co.ogauthority.pwa.service.enums.workflow.assignment;

import uk.co.ogauthority.pwa.service.enums.users.UserType;

public enum WorkflowAssignment {

  CASE_OFFICER(UserType.OGA);

  private final UserType userType;

  WorkflowAssignment(UserType userType) {
    this.userType = userType;
  }

  public UserType getUserType() {
    return userType;
  }

}
