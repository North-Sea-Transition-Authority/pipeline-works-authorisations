package uk.co.ogauthority.pwa.temp.model.form;

public class ProjectInformationForm {

  private Integer workStartDay;
  private Integer workStartMonth;
  private Integer workStartYear;

  private Integer earliestCompletionDay;
  private Integer earliestCompletionMonth;
  private Integer earliestCompletionYear;

  private Integer latestCompletionDay;
  private Integer latestCompletionMonth;
  private Integer latestCompletionYear;

  private String field;
  private String description;

  public Integer getWorkStartDay() {
    return workStartDay;
  }

  public void setWorkStartDay(Integer workStartDay) {
    this.workStartDay = workStartDay;
  }

  public Integer getWorkStartMonth() {
    return workStartMonth;
  }

  public void setWorkStartMonth(Integer workStartMonth) {
    this.workStartMonth = workStartMonth;
  }

  public Integer getWorkStartYear() {
    return workStartYear;
  }

  public void setWorkStartYear(Integer workStartYear) {
    this.workStartYear = workStartYear;
  }

  public Integer getEarliestCompletionDay() {
    return earliestCompletionDay;
  }

  public void setEarliestCompletionDay(Integer earliestCompletionDay) {
    this.earliestCompletionDay = earliestCompletionDay;
  }

  public Integer getEarliestCompletionMonth() {
    return earliestCompletionMonth;
  }

  public void setEarliestCompletionMonth(Integer earliestCompletionMonth) {
    this.earliestCompletionMonth = earliestCompletionMonth;
  }

  public Integer getEarliestCompletionYear() {
    return earliestCompletionYear;
  }

  public void setEarliestCompletionYear(Integer earliestCompletionYear) {
    this.earliestCompletionYear = earliestCompletionYear;
  }

  public Integer getLatestCompletionDay() {
    return latestCompletionDay;
  }

  public void setLatestCompletionDay(Integer latestCompletionDay) {
    this.latestCompletionDay = latestCompletionDay;
  }

  public Integer getLatestCompletionMonth() {
    return latestCompletionMonth;
  }

  public void setLatestCompletionMonth(Integer latestCompletionMonth) {
    this.latestCompletionMonth = latestCompletionMonth;
  }

  public Integer getLatestCompletionYear() {
    return latestCompletionYear;
  }

  public void setLatestCompletionYear(Integer latestCompletionYear) {
    this.latestCompletionYear = latestCompletionYear;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
