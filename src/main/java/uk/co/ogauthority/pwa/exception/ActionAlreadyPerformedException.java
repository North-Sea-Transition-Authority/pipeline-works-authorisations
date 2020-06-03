package uk.co.ogauthority.pwa.exception;

/**
 * Exception to be used when a user is trying to do something that has already been done (e.g. clicked button twice,
 * two users trying to do the same thing in parallel etc).
 */
public class ActionAlreadyPerformedException extends RuntimeException {

  public ActionAlreadyPerformedException(String message) {
    super(message);
  }

  public ActionAlreadyPerformedException(String message, Throwable cause) {
    super(message, cause);
  }

}
