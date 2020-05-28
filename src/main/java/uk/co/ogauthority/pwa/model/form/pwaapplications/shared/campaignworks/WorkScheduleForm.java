package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks;

import java.util.ArrayList;
import java.util.List;

public class WorkScheduleForm {

  private String workStartDay;
  private String workStartMonth;
  private String workStartYear;

  private String workEndDay;
  private String workEndMonth;
  private String workEndYear;

  private List<Integer> padPipelineIds = new ArrayList<>();

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

  public String getWorkEndDay() {
    return workEndDay;
  }

  public void setWorkEndDay(String workEndDay) {
    this.workEndDay = workEndDay;
  }

  public String getWorkEndMonth() {
    return workEndMonth;
  }

  public void setWorkEndMonth(String workEndMonth) {
    this.workEndMonth = workEndMonth;
  }

  public String getWorkEndYear() {
    return workEndYear;
  }

  public void setWorkEndYear(String workEndYear) {
    this.workEndYear = workEndYear;
  }

  public List<Integer> getPadPipelineIds() {
    return padPipelineIds;
  }

  public void setPadPipelineIds(List<Integer> padPipelineIds) {
    this.padPipelineIds = padPipelineIds;
  }
}
