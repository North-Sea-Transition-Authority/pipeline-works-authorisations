package uk.co.ogauthority.pwa.util.forminputs.twofielddate;

import java.time.LocalDate;

public final class OnOrAfterDateHint {
  private final LocalDate date;
  private final String dateLabel;

  public OnOrAfterDateHint(LocalDate date, String dateLabel) {
    this.date = date;
    this.dateLabel = dateLabel;
  }

  public LocalDate getDate() {
    return date;
  }

  public String getDateLabel() {
    return dateLabel;
  }
}
