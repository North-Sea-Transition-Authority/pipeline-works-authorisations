package uk.co.ogauthority.pwa.util.forminputs.twofielddate;

import com.google.common.annotations.VisibleForTesting;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a two field date commonly used on forms and provides access to common operations that might be applied to that date.
 * e.g testing if a given date is before or after etc.
 */
public class TwoFieldDateInput {
  private static final Logger LOGGER = LoggerFactory.getLogger(TwoFieldDateInput.class);

  private static final int DEFAULT_DAY = 1;

  private String month;
  private String year;

  public TwoFieldDateInput() {
  }

  public TwoFieldDateInput(LocalDate localDate) {
    this.year = String.valueOf(localDate.getYear());
    this.month = String.valueOf(localDate.getMonthValue());
  }

  public TwoFieldDateInput(Integer year, Integer month) {
    if (month != null && year != null) {
      this.year = String.valueOf(year);
      this.month = String.valueOf(month);
    }
  }

  public String getMonth() {
    return month;
  }

  public void setMonth(String month) {
    this.month = month;
  }

  public void setMonth(int month) {
    this.month = String.valueOf(month);
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public void setYear(int year) {
    this.year = String.valueOf(year);
  }

  public LocalDate createDateOrNull() {
    return this.createDate()
        .orElse(null);
  }

  public Optional<LocalDate> createDate() {

    try {
      var createdDate = LocalDate.of(Integer.valueOf(year), Integer.valueOf(month), DEFAULT_DAY);
      return Optional.of(createdDate);
    } catch (NumberFormatException e) {
      LOGGER.debug("Could not convert date values to valid numbers. " + this.toString(), e);
      return Optional.empty();
    } catch (DateTimeException e) {
      LOGGER.debug("Could not convert date values to valid date. " + this.toString(), e);
      return Optional.empty();
    }
  }

  public boolean isBefore(LocalDate testDate) {
    var testableDate = testDate.withDayOfMonth(DEFAULT_DAY);

    return this.createDate()
        .filter(date -> date.isBefore(testableDate))
        .isPresent();

  }

  public boolean isAfter(LocalDate testDate) {
    var testableDate = testDate.withDayOfMonth(DEFAULT_DAY);
    return this.createDate()
        .filter(date -> date.isAfter(testableDate))
        .isPresent();
  }

  public boolean isInSameMonth(LocalDate testDate) {
    var testableDate = testDate.withDayOfMonth(DEFAULT_DAY);
    return this.createDate()
        .filter(date -> date.equals(testableDate))
        .isPresent();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TwoFieldDateInput that = (TwoFieldDateInput) o;
    return Objects.equals(month, that.month)
        && Objects.equals(year, that.year);
  }

  @Override
  public int hashCode() {
    return Objects.hash(month, year);
  }

  @Override
  public String toString() {
    return "TwoFieldDateInput{" +
        "month='" + month + '\'' +
        ", year='" + year + '\'' +
        '}';
  }
}
