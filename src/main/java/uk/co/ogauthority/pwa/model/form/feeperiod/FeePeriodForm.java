package uk.co.ogauthority.pwa.model.form.feeperiod;

import java.util.Map;

public class FeePeriodForm {

  private String id;

  private String periodDescription;

  private String periodStartDate;

  private Map<String, String> applicationCostMap;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPeriodDescription() {
    return periodDescription;
  }

  public void setPeriodDescription(String periodDescription) {
    this.periodDescription = periodDescription;
  }

  public String getPeriodStartDate() {
    return periodStartDate;
  }

  public void setPeriodStartDate(String periodStartDate) {
    this.periodStartDate = periodStartDate;
  }

  public Map<String, String> getApplicationCostMap() {
    return applicationCostMap;
  }

  public void setApplicationCostMap(Map<String, String> applicationCostMap) {
    this.applicationCostMap = applicationCostMap;
  }
}
