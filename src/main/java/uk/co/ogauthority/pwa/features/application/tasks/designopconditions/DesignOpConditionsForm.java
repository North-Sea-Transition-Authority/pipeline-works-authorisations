package uk.co.ogauthority.pwa.features.application.tasks.designopconditions;


import java.util.Objects;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;

public class DesignOpConditionsForm {

  private MinMaxInput temperatureOpMinMax;
  private MinMaxInput temperatureDesignMinMax;
  private MinMaxInput pressureOpMinMax;
  private String pressureDesignMax;
  private MinMaxInput flowrateOpMinMax;
  private MinMaxInput flowrateDesignMinMax;

  private MinMaxInput co2Density;
  private String uvalueDesign;

  public DesignOpConditionsForm() {
    this.temperatureOpMinMax = new MinMaxInput();
    this.temperatureDesignMinMax = new MinMaxInput();
    this.pressureOpMinMax = new MinMaxInput();
    this.flowrateOpMinMax = new MinMaxInput();
    this.flowrateDesignMinMax = new MinMaxInput();
    this.co2Density = new MinMaxInput();
  }

  public MinMaxInput getTemperatureOpMinMax() {
    return temperatureOpMinMax;
  }

  public void setTemperatureOpMinMax(MinMaxInput temperatureOpMinMax) {
    this.temperatureOpMinMax = temperatureOpMinMax;
  }

  public MinMaxInput getTemperatureDesignMinMax() {
    return temperatureDesignMinMax;
  }

  public void setTemperatureDesignMinMax(MinMaxInput temperatureDesignMinMax) {
    this.temperatureDesignMinMax = temperatureDesignMinMax;
  }

  public MinMaxInput getPressureOpMinMax() {
    return pressureOpMinMax;
  }

  public void setPressureOpMinMax(MinMaxInput pressureOpMinMax) {
    this.pressureOpMinMax = pressureOpMinMax;
  }

  public String getPressureDesignMax() {
    return pressureDesignMax;
  }

  public void setPressureDesignMax(String pressureDesignMax) {
    this.pressureDesignMax = pressureDesignMax;
  }

  public MinMaxInput getFlowrateOpMinMax() {
    return flowrateOpMinMax;
  }

  public void setFlowrateOpMinMax(MinMaxInput flowrateOpMinMax) {
    this.flowrateOpMinMax = flowrateOpMinMax;
  }

  public MinMaxInput getFlowrateDesignMinMax() {
    return flowrateDesignMinMax;
  }

  public void setFlowrateDesignMinMax(MinMaxInput flowrateDesignMinMax) {
    this.flowrateDesignMinMax = flowrateDesignMinMax;
  }

  public MinMaxInput getCo2Density() {
    return co2Density;
  }

  public DesignOpConditionsForm setCo2Density(MinMaxInput co2Density) {
    this.co2Density = co2Density;
    return this;
  }

  public String getUvalueDesign() {
    return uvalueDesign;
  }

  public void setUvalueDesign(String uvalueDesign) {
    this.uvalueDesign = uvalueDesign;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesignOpConditionsForm that = (DesignOpConditionsForm) o;
    return Objects.equals(temperatureOpMinMax, that.temperatureOpMinMax)
        && Objects.equals(temperatureDesignMinMax, that.temperatureDesignMinMax)
        && Objects.equals(pressureOpMinMax, that.pressureOpMinMax)
        && Objects.equals(pressureDesignMax, that.pressureDesignMax)
        && Objects.equals(flowrateOpMinMax, that.flowrateOpMinMax)
        && Objects.equals(flowrateDesignMinMax, that.flowrateDesignMinMax)
        && Objects.equals(uvalueDesign, that.uvalueDesign);
  }

  @Override
  public int hashCode() {
    return Objects.hash(temperatureOpMinMax, temperatureDesignMinMax, pressureOpMinMax,
        pressureDesignMax, flowrateOpMinMax, flowrateDesignMinMax, uvalueDesign);
  }


}
