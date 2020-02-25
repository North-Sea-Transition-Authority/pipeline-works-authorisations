package uk.co.ogauthority.pwa.temp.model.view.pipelinetechnical;

import java.io.Serializable;

public class DesignOperatingLimitsView implements Serializable {
  private String title;
  private String unit;
  private String min;
  private String max;

  public DesignOperatingLimitsView(String title, String unit, String min, String max) {
    this.title = title;
    this.unit = unit;
    this.min = min;
    this.max = max;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getMin() {
    return min;
  }

  public void setMin(String min) {
    this.min = min;
  }

  public String getMax() {
    return max;
  }

  public void setMax(String max) {
    this.max = max;
  }
}
