package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo;


import java.util.Objects;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;

public class DesignOpConditionsForm {

  private MinMaxInput temperatureOpMinMax;
  private MinMaxInput temperatureDesignMinMax;
  private MinMaxInput pressureOpMinMax;
  private MinMaxInput pressureDesignMinMax;
  private MinMaxInput flowrateOpMinMax;
  private MinMaxInput flowrateDesignMinMax;
  private String uvalueOp;
  private String uvalueDesign;


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

  public MinMaxInput getPressureDesignMinMax() {
    return pressureDesignMinMax;
  }

  public void setPressureDesignMinMax(MinMaxInput pressureDesignMinMax) {
    this.pressureDesignMinMax = pressureDesignMinMax;
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

  public String getUvalueOp() {
    return uvalueOp;
  }

  public void setUvalueOp(String uvalueOp) {
    this.uvalueOp = uvalueOp;
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
        && Objects.equals(pressureDesignMinMax, that.pressureDesignMinMax)
        && Objects.equals(flowrateOpMinMax, that.flowrateOpMinMax)
        && Objects.equals(flowrateDesignMinMax, that.flowrateDesignMinMax)
        && Objects.equals(uvalueOp, that.uvalueOp)
        && Objects.equals(uvalueDesign, that.uvalueDesign);
  }

  @Override
  public int hashCode() {
    return Objects.hash(temperatureOpMinMax, temperatureDesignMinMax, pressureOpMinMax, pressureDesignMinMax,
        flowrateOpMinMax, flowrateDesignMinMax, uvalueOp, uvalueDesign);
  }
}
