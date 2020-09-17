package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadDesignOpConditions;

public class DesignOpConditionsView {

  private MinMaxView temperatureOpMinMaxView;
  private MinMaxView temperatureDesignMinMaxView;
  private MinMaxView pressureOpMinMaxView;
  private MinMaxView pressureDesignMinMaxView;
  private MinMaxView flowrateOpMinMaxView;
  private MinMaxView flowrateDesignMinMaxView;
  private String uvalueOp;
  private String uvalueDesign;



  public DesignOpConditionsView(PadDesignOpConditions padDesignOpConditions) {
    this.temperatureOpMinMaxView = MinMaxView.createMinMaxView(
        getStringValue(padDesignOpConditions.getTemperatureOpMinValue()),
        getStringValue(padDesignOpConditions.getTemperatureOpMaxValue()),
        UnitMeasurement.DEGREES_CELSIUS);

    this.temperatureDesignMinMaxView = MinMaxView.createMinMaxView(
        getStringValue(padDesignOpConditions.getTemperatureDesignMinValue()),
        getStringValue(padDesignOpConditions.getTemperatureDesignMaxValue()),
        UnitMeasurement.DEGREES_CELSIUS);

    this.pressureOpMinMaxView = MinMaxView.createInternalExternalView(
        getStringValue(padDesignOpConditions.getPressureOpInternalValue()),
        getStringValue(padDesignOpConditions.getPressureOpExternalValue()),
        UnitMeasurement.BAR_G);

    this.pressureDesignMinMaxView = MinMaxView.createInternalExternalView(
        getStringValue(padDesignOpConditions.getPressureDesignInternalValue()),
        getStringValue(padDesignOpConditions.getPressureDesignExternalValue()),
        UnitMeasurement.BAR_G);

    this.flowrateOpMinMaxView = MinMaxView.createMinMaxView(
        getStringValue(padDesignOpConditions.getFlowrateOpMinValue()),
        getStringValue(padDesignOpConditions.getFlowrateOpMaxValue()),
        UnitMeasurement.KSCM_D);

    this.flowrateDesignMinMaxView = MinMaxView.createMinMaxView(
        getStringValue(padDesignOpConditions.getFlowrateDesignMinValue()),
        getStringValue(padDesignOpConditions.getFlowrateDesignMaxValue()),
        UnitMeasurement.KSCM_D);

    this.uvalueOp = getStringValue(padDesignOpConditions.getUvalueOp());
    this.uvalueDesign = getStringValue(padDesignOpConditions.getUvalueDesign());
  }


  public MinMaxView getTemperatureOpMinMaxView() {
    return temperatureOpMinMaxView;
  }

  public MinMaxView getTemperatureDesignMinMaxView() {
    return temperatureDesignMinMaxView;
  }

  public MinMaxView getPressureOpMinMaxView() {
    return pressureOpMinMaxView;
  }

  public MinMaxView getPressureDesignMinMaxView() {
    return pressureDesignMinMaxView;
  }

  public MinMaxView getFlowrateOpMinMaxView() {
    return flowrateOpMinMaxView;
  }

  public MinMaxView getFlowrateDesignMinMaxView() {
    return flowrateDesignMinMaxView;
  }

  public String getUvalueOp() {
    return uvalueOp;
  }

  public String getUvalueDesign() {
    return uvalueDesign;
  }

  private String getStringValue(BigDecimal value) {
    return value == null ? null : String.valueOf(value);
  }

}
