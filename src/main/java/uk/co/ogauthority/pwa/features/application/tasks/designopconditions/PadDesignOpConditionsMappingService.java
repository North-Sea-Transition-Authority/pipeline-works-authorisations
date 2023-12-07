package uk.co.ogauthority.pwa.features.application.tasks.designopconditions;

import java.math.BigDecimal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;


@Service
public class PadDesignOpConditionsMappingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      PadDesignOpConditionsMappingService.class);

  public void mapEntityToForm(DesignOpConditionsForm form, PadDesignOpConditions entity) {
    form.setPressureOpMinMax(
        new MinMaxInput(
            getStringValue(entity.getPressureOpMinValue()),
            getStringValue(entity.getPressureOpMaxValue())));
    form.setPressureDesignMax(getStringValue(entity.getPressureDesignMaxValue()));
    form.setTemperatureOpMinMax(
        new MinMaxInput(
            getStringValue(entity.getTemperatureOpMinValue()),
            getStringValue(entity.getTemperatureOpMaxValue())));
    form.setTemperatureDesignMinMax(
        new MinMaxInput(
            getStringValue(entity.getTemperatureDesignMinValue()),
            getStringValue(entity.getTemperatureDesignMaxValue())));
    form.setFlowrateOpMinMax(
        new MinMaxInput(
            getStringValue(entity.getFlowrateOpMinValue()),
            getStringValue(entity.getFlowrateOpMaxValue())));
    form.setFlowrateDesignMinMax(
        new MinMaxInput(
            getStringValue(entity.getFlowrateDesignMinValue()),
            getStringValue(entity.getFlowrateDesignMaxValue())));
    form.setCo2Density(
        new MinMaxInput(
            getStringValue(entity.getCo2DensityMinValue()),
            getStringValue(entity.getCo2DensityMaxValue())));
    form.setUvalueDesign(getStringValue(entity.getUvalueDesign()));
  }



  public void mapFormToEntity(DesignOpConditionsForm form, PadDesignOpConditions entity) {
    entity.setTemperatureOpMinValue(form.getTemperatureOpMinMax().createMinOrNull());
    entity.setTemperatureOpMaxValue(form.getTemperatureOpMinMax().createMaxOrNull());
    entity.setTemperatureDesignMinValue(form.getTemperatureDesignMinMax().createMinOrNull());
    entity.setTemperatureDesignMaxValue(form.getTemperatureDesignMinMax().createMaxOrNull());

    entity.setPressureOpMinValue(form.getPressureOpMinMax().createMinOrNull());
    entity.setPressureOpMaxValue(form.getPressureOpMinMax().createMaxOrNull());
    entity.setPressureDesignMaxValue(createBigDecimal(form.getPressureDesignMax()).orElse(null));

    entity.setFlowrateOpMinValue(form.getFlowrateOpMinMax().createMinOrNull());
    entity.setFlowrateOpMaxValue(form.getFlowrateOpMinMax().createMaxOrNull());
    entity.setFlowrateDesignMinValue(form.getFlowrateDesignMinMax().createMinOrNull());
    entity.setFlowrateDesignMaxValue(form.getFlowrateDesignMinMax().createMaxOrNull());

    entity.setCo2DensityMaxValue(form.getCo2Density().createMaxOrNull());
    entity.setCo2DensityMinValue(form.getCo2Density().createMinOrNull());

    entity.setUvalueDesign(createBigDecimal(form.getUvalueDesign()).orElse(null));
  }


  private String getStringValue(BigDecimal value) {
    return value == null ? null : String.valueOf(value);
  }


  private Optional<BigDecimal> createBigDecimal(String valueStr) {
    try {
      var createdNum = valueStr != null ? new BigDecimal(valueStr) : null;
      return Optional.ofNullable(createdNum);
    } catch (NumberFormatException e) {
      LOGGER.debug("Could not convert minimum/maximum values to valid numbers. " + this.toString(), e);
      return Optional.empty();
    }
  }



}

