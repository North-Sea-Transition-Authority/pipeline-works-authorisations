package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.util.Objects;

public class DesignOpConditionsView {

  private PairValueView temperatureOpPairValue;
  private PairValueView temperatureDesignPairValue;
  private PairValueView pressureOpPairValue;
  private PairValueView pressureDesignPairValue;
  private PairValueView flowrateOpPairValue;
  private PairValueView flowrateDesignPairValue;
  private String uvalueOp;
  private String uvalueDesign;


  public DesignOpConditionsView(
      PairValueView temperatureOpPairValue,
      PairValueView temperatureDesignPairValue,
      PairValueView pressureOpPairValue,
      PairValueView pressureDesignPairValue,
      PairValueView flowrateOpPairValue,
      PairValueView flowrateDesignPairValue, String uvalueOp, String uvalueDesign) {
    this.temperatureOpPairValue = temperatureOpPairValue;
    this.temperatureDesignPairValue = temperatureDesignPairValue;
    this.pressureOpPairValue = pressureOpPairValue;
    this.pressureDesignPairValue = pressureDesignPairValue;
    this.flowrateOpPairValue = flowrateOpPairValue;
    this.flowrateDesignPairValue = flowrateDesignPairValue;
    this.uvalueOp = uvalueOp;
    this.uvalueDesign = uvalueDesign;
  }


  public PairValueView getTemperatureOpPairValue() {
    return temperatureOpPairValue;
  }

  public PairValueView getTemperatureDesignPairValue() {
    return temperatureDesignPairValue;
  }

  public PairValueView getPressureOpPairValue() {
    return pressureOpPairValue;
  }

  public PairValueView getPressureDesignPairValue() {
    return pressureDesignPairValue;
  }

  public PairValueView getFlowrateOpPairValue() {
    return flowrateOpPairValue;
  }

  public PairValueView getFlowrateDesignPairValue() {
    return flowrateDesignPairValue;
  }

  public String getUvalueOp() {
    return uvalueOp;
  }

  public String getUvalueDesign() {
    return uvalueDesign;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesignOpConditionsView that = (DesignOpConditionsView) o;
    return Objects.equals(temperatureOpPairValue, that.temperatureOpPairValue)
        && Objects.equals(temperatureDesignPairValue, that.temperatureDesignPairValue)
        && Objects.equals(pressureOpPairValue, that.pressureOpPairValue)
        && Objects.equals(pressureDesignPairValue, that.pressureDesignPairValue)
        && Objects.equals(flowrateOpPairValue, that.flowrateOpPairValue)
        && Objects.equals(flowrateDesignPairValue, that.flowrateDesignPairValue)
        && Objects.equals(uvalueOp, that.uvalueOp)
        && Objects.equals(uvalueDesign, that.uvalueDesign);
  }

  @Override
  public int hashCode() {
    return Objects.hash(temperatureOpPairValue, temperatureDesignPairValue, pressureOpPairValue,
        pressureDesignPairValue,
        flowrateOpPairValue, flowrateDesignPairValue, uvalueOp, uvalueDesign);
  }
}
