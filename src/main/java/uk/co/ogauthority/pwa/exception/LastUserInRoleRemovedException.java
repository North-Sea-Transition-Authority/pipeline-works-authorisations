package uk.co.ogauthority.pwa.exception;

public class LastUserInRoleRemovedException extends RuntimeException {

  /**
   * Message should include the roles that would be empty if action continued.
   */
  public LastUserInRoleRemovedException(String message) {
    super(message);
  }

}
