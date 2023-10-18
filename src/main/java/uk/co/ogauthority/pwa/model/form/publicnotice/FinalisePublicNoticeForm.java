package uk.co.ogauthority.pwa.model.form.publicnotice;


import java.time.LocalDate;
import java.util.Optional;

public class FinalisePublicNoticeForm {

  private Integer startDay;
  private Integer startMonth;
  private Integer startYear;
  private Integer daysToBePublishedFor = 28;

  private String dateChangeReason;


  public Integer getStartDay() {
    return startDay;
  }

  public void setStartDay(Integer startDay) {
    this.startDay = startDay;
  }

  public Integer getStartMonth() {
    return startMonth;
  }

  public void setStartMonth(Integer startMonth) {
    this.startMonth = startMonth;
  }

  public Integer getStartYear() {
    return startYear;
  }

  public void setStartYear(Integer startYear) {
    this.startYear = startYear;
  }

  public Optional<LocalDate> getLocalDate() {
    if (startDay == null || startMonth == null || startYear == null) {
      return Optional.empty();
    }
    return Optional.of(LocalDate.of(getStartYear(), getStartMonth(), getStartDay()));
  }

  public Integer getDaysToBePublishedFor() {
    return daysToBePublishedFor;
  }

  public void setDaysToBePublishedFor(Integer daysToBePublishedFor) {
    this.daysToBePublishedFor = daysToBePublishedFor;
  }

  public String getDateChangeReason() {
    return dateChangeReason;
  }

  public FinalisePublicNoticeForm setDateChangeReason(String dateChangeReason) {
    this.dateChangeReason = dateChangeReason;
    return this;
  }
}
