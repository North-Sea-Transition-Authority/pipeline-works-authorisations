package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipielinetechinfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import org.apache.commons.lang3.RandomUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadDesignOpConditions;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadDesignOpConditions_;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;


public class PadDesignOpConditionsTestUtil {

  private PadDesignOpConditionsTestUtil() {
    // no instantiation
  }

  public static DesignOpConditionsForm createBlankForm() {
    var form = new DesignOpConditionsForm();
    form.setTemperatureOpMinMax(new MinMaxInput());
    form.setTemperatureDesignMinMax(new MinMaxInput());
    form.setPressureOpMinMax(new MinMaxInput());
    form.setFlowrateOpMinMax(new MinMaxInput());
    form.setFlowrateDesignMinMax(new MinMaxInput());
    return form;
  }

  public static DesignOpConditionsForm createValidForm() {
    var form = new DesignOpConditionsForm();
    form.setTemperatureOpMinMax(new MinMaxInput("1", "2"));
    form.setTemperatureDesignMinMax(new MinMaxInput("3", "4"));
    form.setPressureOpMinMax(new MinMaxInput("5", "6"));
    form.setPressureDesignMax("7");
    form.setFlowrateOpMinMax(new MinMaxInput("9", "10"));
    form.setFlowrateDesignMinMax(new MinMaxInput("11", "12"));
    form.setUvalueDesign("14");
    return form;
  }


  public static PadDesignOpConditions createValidEntity() {
    var entity = new PadDesignOpConditions();
    var form = createValidForm();

    entity.setTemperatureOpMinValue(form.getTemperatureOpMinMax().createMinOrNull());
    entity.setTemperatureOpMaxValue(form.getTemperatureOpMinMax().createMaxOrNull());
    entity.setTemperatureDesignMinValue(form.getTemperatureDesignMinMax().createMinOrNull());
    entity.setTemperatureDesignMaxValue(form.getTemperatureDesignMinMax().createMaxOrNull());
    entity.setPressureOpMinValue(form.getPressureOpMinMax().createMinOrNull());
    entity.setPressureOpMaxValue(form.getPressureOpMinMax().createMaxOrNull());
    entity.setPressureDesignMaxValue(form.getPressureDesignMax() != null ? new BigDecimal(form.getPressureDesignMax()) : null);
    entity.setFlowrateOpMinValue(form.getFlowrateOpMinMax().createMinOrNull());
    entity.setFlowrateOpMaxValue(form.getFlowrateOpMinMax().createMaxOrNull());
    entity.setFlowrateDesignMinValue(form.getFlowrateDesignMinMax().createMinOrNull());
    entity.setFlowrateDesignMaxValue(form.getFlowrateDesignMinMax().createMaxOrNull());
    entity.setUvalueDesign(form.getUvalueDesign() != null ? new BigDecimal(form.getUvalueDesign()) : null);

    return entity;
  }

  public static PadDesignOpConditions createPadDesignOpConditions(PwaApplicationDetail pwaApplicationDetail) {
    var oc = new PadDesignOpConditions(pwaApplicationDetail);
    oc.setTemperatureOpMinValue(
        BigDecimal.valueOf(RandomUtils.nextDouble(0, 50)).setScale(2, RoundingMode.HALF_UP));
    oc.setTemperatureOpMaxValue(
        BigDecimal.valueOf(RandomUtils.nextDouble(50, 100)).setScale(2, RoundingMode.HALF_UP));
    oc.setTemperatureDesignMinValue(
        BigDecimal.valueOf(RandomUtils.nextDouble(0, 50)).setScale(2, RoundingMode.HALF_UP));
    oc.setTemperatureDesignMaxValue(
        BigDecimal.valueOf(RandomUtils.nextDouble(50, 100)).setScale(2, RoundingMode.HALF_UP));
    oc.setPressureOpMinValue(
        BigDecimal.valueOf(RandomUtils.nextDouble(0, 50)).setScale(2, RoundingMode.HALF_UP));
    oc.setPressureOpMaxValue(
        BigDecimal.valueOf(RandomUtils.nextDouble(50, 100)).setScale(2, RoundingMode.HALF_UP));
    oc.setPressureDesignMaxValue(
        BigDecimal.valueOf(RandomUtils.nextDouble(50, 100)).setScale(2, RoundingMode.HALF_UP));
    oc.setFlowrateOpMinValue(BigDecimal.valueOf(RandomUtils.nextDouble(0, 50)).setScale(2, RoundingMode.HALF_UP));
    oc.setFlowrateOpMaxValue(BigDecimal.valueOf(RandomUtils.nextDouble(50, 100)).setScale(2, RoundingMode.HALF_UP));
    oc.setFlowrateDesignMinValue(BigDecimal.valueOf(RandomUtils.nextDouble(0, 50)).setScale(2, RoundingMode.HALF_UP));
    oc.setFlowrateDesignMaxValue(BigDecimal.valueOf(RandomUtils.nextDouble(50, 100)).setScale(2, RoundingMode.HALF_UP));
    oc.setUvalueDesign(BigDecimal.valueOf(RandomUtils.nextDouble(0, 100)).setScale(2, RoundingMode.HALF_UP));

    ObjectTestUtils.assertAllFieldsNotNull(
        oc,
        PadDesignOpConditions.class,
        Set.of(PadDesignOpConditions_.ID)
    );

    return oc;


  }

}