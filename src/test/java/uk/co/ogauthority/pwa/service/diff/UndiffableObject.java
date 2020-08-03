package uk.co.ogauthority.pwa.service.diff;

import java.time.Instant;

public class UndiffableObject {

  private Instant time;

  public UndiffableObject(Instant time) {
    this.time = time;
  }

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }
}
