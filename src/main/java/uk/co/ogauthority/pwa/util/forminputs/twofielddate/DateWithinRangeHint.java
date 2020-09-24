package uk.co.ogauthority.pwa.util.forminputs.twofielddate;

import java.time.LocalDate;

public final class DateWithinRangeHint {

  private final LocalDate fromDate;
  private final LocalDate toDate;
  private final String rangeDescription;


  public DateWithinRangeHint(LocalDate fromDate, LocalDate toDate, String rangeDescription) {
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.rangeDescription = rangeDescription;
  }


  public LocalDate getFromDate() {
    return fromDate;
  }

  public LocalDate getToDate() {
    return toDate;
  }

  public String getRangeDescription() {
    return rangeDescription;
  }
}
