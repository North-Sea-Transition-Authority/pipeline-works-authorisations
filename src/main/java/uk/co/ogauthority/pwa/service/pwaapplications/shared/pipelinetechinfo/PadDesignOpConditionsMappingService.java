package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadDesignOpConditions;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.DesignOpConditionsView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PairValueView;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;


@Service
public class PadDesignOpConditionsMappingService {

  public void mapEntityToForm(DesignOpConditionsForm form, PadDesignOpConditions entity) {
    form.setTemperatureOpMinMax(new MinMaxInput(
        getStringValue(entity.getTemperatureOpMinValue()), getStringValue(entity.getTemperatureOpMaxValue())));
    form.setTemperatureDesignMinMax(new MinMaxInput(
        getStringValue(entity.getTemperatureDesignMinValue()), getStringValue(entity.getTemperatureDesignMaxValue())));

    form.setPressureOpInternalExternal(new MinMaxInput(
        getStringValue(entity.getPressureOpInternalValue()), getStringValue(entity.getPressureOpExternalValue())));
    form.setPressureDesignInternalExternal(new MinMaxInput(
        getStringValue(entity.getPressureDesignInternalValue()), getStringValue(entity.getPressureDesignExternalValue())));

    form.setFlowrateOpMinMax(new MinMaxInput(
        getStringValue(entity.getFlowrateOpMinValue()), getStringValue(entity.getFlowrateOpMaxValue())));
    form.setFlowrateDesignMinMax(new MinMaxInput(
        getStringValue(entity.getFlowrateDesignMinValue()), getStringValue(entity.getFlowrateDesignMaxValue())));

    form.setUvalueOp(getStringValue(entity.getUvalueOp()));
    form.setUvalueDesign(getStringValue(entity.getUvalueDesign()));
  }



  public void mapFormToEntity(DesignOpConditionsForm form, PadDesignOpConditions entity) {
    entity.setTemperatureOpMinValue(form.getTemperatureOpMinMax().createMinOrNull());
    entity.setTemperatureOpMaxValue(form.getTemperatureOpMinMax().createMaxOrNull());
    entity.setTemperatureDesignMinValue(form.getTemperatureDesignMinMax().createMinOrNull());
    entity.setTemperatureDesignMaxValue(form.getTemperatureDesignMinMax().createMaxOrNull());

    entity.setPressureOpInternalValue(form.getPressureOpInternalExternal().createMinOrNull());
    entity.setPressureOpExternalValue(form.getPressureOpInternalExternal().createMaxOrNull());
    entity.setPressureDesignInternalValue(form.getPressureDesignInternalExternal().createMinOrNull());
    entity.setPressureDesignExternalValue(form.getPressureDesignInternalExternal().createMaxOrNull());

    entity.setFlowrateOpMinValue(form.getFlowrateOpMinMax().createMinOrNull());
    entity.setFlowrateOpMaxValue(form.getFlowrateOpMinMax().createMaxOrNull());
    entity.setFlowrateDesignMinValue(form.getFlowrateDesignMinMax().createMinOrNull());
    entity.setFlowrateDesignMaxValue(form.getFlowrateDesignMinMax().createMaxOrNull());

    entity.setUvalueOp(form.getUvalueOp() != null ? new BigDecimal(form.getUvalueOp()) : null);
    entity.setUvalueDesign(form.getUvalueDesign() != null ? new BigDecimal(form.getUvalueDesign()) : null);
  }


  public DesignOpConditionsView createViewFromEntity(PadDesignOpConditions entity) {

    return new DesignOpConditionsView(
        new PairValueView(getStringValue(entity.getTemperatureOpMinValue()), getStringValue(entity.getTemperatureOpMaxValue())),
        new PairValueView(getStringValue(entity.getTemperatureDesignMinValue()), getStringValue(entity.getTemperatureDesignMaxValue())),
        new PairValueView(getStringValue(entity.getPressureOpInternalValue()), getStringValue(entity.getPressureOpExternalValue())),
        new PairValueView(getStringValue(entity.getPressureDesignInternalValue()), getStringValue(entity.getPressureDesignExternalValue())),
        new PairValueView(getStringValue(entity.getFlowrateOpMinValue()), getStringValue(entity.getFlowrateOpMaxValue())),
        new PairValueView(getStringValue(entity.getFlowrateDesignMinValue()), getStringValue(entity.getFlowrateDesignMaxValue())),
        getStringValue(entity.getUvalueOp()),
        getStringValue(entity.getUvalueDesign())
    );
  }


  private String getStringValue(BigDecimal value) {
    return value == null ? null : String.valueOf(value);
  }



}

