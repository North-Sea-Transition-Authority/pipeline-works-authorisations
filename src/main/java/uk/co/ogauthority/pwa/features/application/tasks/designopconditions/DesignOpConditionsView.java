package uk.co.ogauthority.pwa.features.application.tasks.designopconditions;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.MinMaxView;

public class DesignOpConditionsView {

  private MinMaxView temperatureOpMinMaxView;
  private MinMaxView temperatureDesignMinMaxView;
  private MinMaxView pressureOpMinMaxView;
  private String pressureDesignMax;
  private MinMaxView flowrateOpMinMaxView;
  private MinMaxView flowrateDesignMinMaxView;

  private MinMaxView co2DensityView;
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

    this.pressureOpMinMaxView = MinMaxView.createMinMaxView(
        getStringValue(padDesignOpConditions.getPressureOpMinValue()),
        getStringValue(padDesignOpConditions.getPressureOpMaxValue()),
        UnitMeasurement.BAR_G);

    this.pressureDesignMax = getStringValue(padDesignOpConditions.getPressureDesignMaxValue());

    this.flowrateOpMinMaxView = MinMaxView.createMinMaxView(
        getStringValue(padDesignOpConditions.getFlowrateOpMinValue()),
        getStringValue(padDesignOpConditions.getFlowrateOpMaxValue()),
        padDesignOpConditions.getFlowrateMeasurement());

    this.flowrateDesignMinMaxView = MinMaxView.createMinMaxView(
        getStringValue(padDesignOpConditions.getFlowrateDesignMinValue()),
        getStringValue(padDesignOpConditions.getFlowrateDesignMaxValue()),
        padDesignOpConditions.getFlowrateMeasurement());

    this.co2DensityView = MinMaxView.createMinMaxView(
        getStringValue(padDesignOpConditions.getCo2DensityMinValue()),
        getStringValue(padDesignOpConditions.getCo2DensityMaxValue()),
        UnitMeasurement.KG_METRE_CUBED);
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

  public String getPressureDesignMax() {
    return pressureDesignMax;
  }

  public MinMaxView getFlowrateOpMinMaxView() {
    return flowrateOpMinMaxView;
  }

  public MinMaxView getFlowrateDesignMinMaxView() {
    return flowrateDesignMinMaxView;
  }

  public String getUvalueDesign() {
    return uvalueDesign;
  }

  private String getStringValue(BigDecimal value) {
    return value == null ? null : String.valueOf(value);
  }

}
