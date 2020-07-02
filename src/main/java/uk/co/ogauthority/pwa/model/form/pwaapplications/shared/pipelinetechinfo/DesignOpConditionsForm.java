package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo;


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


}
