package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo;


import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;

public class DesignOpConditionsForm {

  private MinMaxInput temperatureOpMinMax;

  private MinMaxInput pressureOpInternalExternal;




  public MinMaxInput getTemperatureOpMinMax() {
    return temperatureOpMinMax;
  }

  public void setTemperatureOpMinMax(MinMaxInput temperatureOpMinMax) {
    this.temperatureOpMinMax = temperatureOpMinMax;
  }

  public MinMaxInput getPressureOpInternalExternal() {
    return pressureOpInternalExternal;
  }

  public void setPressureOpInternalExternal(MinMaxInput pressureOpInternalExternal) {
    this.pressureOpInternalExternal = pressureOpInternalExternal;
  }
}
