package uk.co.ogauthority.pwa.externalapi;

public class RestError {

  private final int status;
  private final String message;

  public RestError(int status,
                   String message) {
    this.status = status;
    this.message = message;
  }

  public int getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
