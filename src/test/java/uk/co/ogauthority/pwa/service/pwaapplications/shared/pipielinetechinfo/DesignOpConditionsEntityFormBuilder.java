package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipielinetechinfo;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadDesignOpConditions;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;


public class DesignOpConditionsEntityFormBuilder {

  public DesignOpConditionsForm createBlankForm() {
    var form = new DesignOpConditionsForm();
    form.setTemperatureOpMinMax(new MinMaxInput());
    form.setTemperatureDesignMinMax(new MinMaxInput());
    form.setPressureOpInternalExternal(new MinMaxInput());
    form.setPressureDesignInternalExternal(new MinMaxInput());
    form.setFlowrateOpMinMax(new MinMaxInput());
    form.setFlowrateDesignMinMax(new MinMaxInput());
    return form;
  }

  public DesignOpConditionsForm createValidForm() {
    var form = new DesignOpConditionsForm();
    form.setTemperatureOpMinMax(new MinMaxInput("1", "2"));
    form.setTemperatureDesignMinMax(new MinMaxInput("3", "4"));
    form.setPressureOpInternalExternal(new MinMaxInput("5", "6"));
    form.setPressureDesignInternalExternal(new MinMaxInput("7", "8"));
    form.setFlowrateOpMinMax(new MinMaxInput("9", "10"));
    form.setFlowrateDesignMinMax(new MinMaxInput("11", "12"));
    form.setUvalueOp("13");
    form.setUvalueDesign("14");
    return form;
  }


  public PadDesignOpConditions createValidEntity() {
    var entity = new PadDesignOpConditions();
    var form = createValidForm();

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

    return entity;
  }

}