package uk.co.ogauthority.pwa.model.form.publicnotice;


public class FinalisePublicNoticeForm {

  private Integer startDay;
  private Integer startMonth;
  private Integer startYear;
  private Integer daysToBePublishedFor = 28;


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

  public Integer getDaysToBePublishedFor() {
    return daysToBePublishedFor;
  }

  public void setDaysToBePublishedFor(Integer daysToBePublishedFor) {
    this.daysToBePublishedFor = daysToBePublishedFor;
  }
}
