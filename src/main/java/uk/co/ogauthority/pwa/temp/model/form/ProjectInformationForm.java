package uk.co.ogauthority.pwa.temp.model.form;

import java.io.Serializable;

public class ProjectInformationForm implements Serializable {

  private String workStartDay;
  private String workStartMonth;
  private String workStartYear;

  private String earliestCompletionDay;
  private String earliestCompletionMonth;
  private String earliestCompletionYear;

  private String latestCompletionDay;
  private String latestCompletionMonth;
  private String latestCompletionYear;

  private String field;
  private String description;

  public String getWorkStartDay() {
    return workStartDay;
  }

  public void setWorkStartDay(String workStartDay) {
    this.workStartDay = workStartDay;
  }

  public String getWorkStartMonth() {
    return workStartMonth;
  }

  public void setWorkStartMonth(String workStartMonth) {
    this.workStartMonth = workStartMonth;
  }

  public String getWorkStartYear() {
    return workStartYear;
  }

  public void setWorkStartYear(String workStartYear) {
    this.workStartYear = workStartYear;
  }

  public String getEarliestCompletionDay() {
    return earliestCompletionDay;
  }

  public void setEarliestCompletionDay(String earliestCompletionDay) {
    this.earliestCompletionDay = earliestCompletionDay;
  }

  public String getEarliestCompletionMonth() {
    return earliestCompletionMonth;
  }

  public void setEarliestCompletionMonth(String earliestCompletionMonth) {
    this.earliestCompletionMonth = earliestCompletionMonth;
  }

  public String getEarliestCompletionYear() {
    return earliestCompletionYear;
  }

  public void setEarliestCompletionYear(String earliestCompletionYear) {
    this.earliestCompletionYear = earliestCompletionYear;
  }

  public String getLatestCompletionDay() {
    return latestCompletionDay;
  }

  public void setLatestCompletionDay(String latestCompletionDay) {
    this.latestCompletionDay = latestCompletionDay;
  }

  public String getLatestCompletionMonth() {
    return latestCompletionMonth;
  }

  public void setLatestCompletionMonth(String latestCompletionMonth) {
    this.latestCompletionMonth = latestCompletionMonth;
  }

  public String getLatestCompletionYear() {
    return latestCompletionYear;
  }

  public void setLatestCompletionYear(String latestCompletionYear) {
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
