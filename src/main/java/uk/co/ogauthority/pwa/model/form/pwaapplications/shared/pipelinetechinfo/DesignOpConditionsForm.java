package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo;


import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;

public class DesignOpConditionsForm {

  private MinMaxInput temperatureOpMinMax;

  private MinMaxInput pressureOpInternalValue;




  public MinMaxInput getTemperatureOpMinMax() {
    return temperatureOpMinMax;
  }

  public void setTemperatureOpMinMax(MinMaxInput temperatureOpMinMax) {
    this.temperatureOpMinMax = temperatureOpMinMax;
  }

  public MinMaxInput getPressureOpInternalValue() {
    return pressureOpInternalValue;
  }

  public void setPressureOpInternalValue(MinMaxInput pressureOpInternalValue) {
    this.pressureOpInternalValue = pressureOpInternalValue;
  }
}
