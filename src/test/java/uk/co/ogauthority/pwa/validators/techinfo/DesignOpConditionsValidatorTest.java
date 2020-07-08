package uk.co.ogauthority.pwa.validators.techinfo;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.MinMaxValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInputValidator;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PadDesignOpConditionsValidator;

@RunWith(MockitoJUnitRunner.class)
public class DesignOpConditionsValidatorTest {

  private MinMaxInputValidator minMaxInputValidator;
  private PadDesignOpConditionsValidator validator;

  @Before
  public void setUp() {
    minMaxInputValidator = new MinMaxInputValidator();
    validator = new PadDesignOpConditionsValidator(minMaxInputValidator);
  }


  private DesignOpConditionsForm createBlankForm() {
    var form = new DesignOpConditionsForm();
    form.setTemperatureOpMinMax(new MinMaxInput());
    form.setTemperatureDesignMinMax(new MinMaxInput());
    form.setPressureOpInternalExternal(new MinMaxInput());
    form.setPressureDesignInternalExternal(new MinMaxInput());
    form.setFlowrateOpMinMax(new MinMaxInput());
    form.setFlowrateDesignMinMax(new MinMaxInput());
    return form;
  }

  @Test
  public void validate_form_empty() {
    var form = createBlankForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("temperatureOpMinMax.minValue", Set.of("minValue.required")),
        entry("temperatureOpMinMax.maxValue", Set.of("maxValue.required")),
        entry("pressureOpInternalExternal.minValue", Set.of("minValue.required")),
        entry("pressureDesignInternalExternal.maxValue", Set.of("maxValue.required")),
        entry("flowrateOpMinMax.minValue", Set.of("minValue.required")),
        entry("flowrateDesignMinMax.maxValue", Set.of("maxValue.required")),
        entry("uvalueOp", Set.of("uvalueOp.required")),
        entry("uvalueDesign", Set.of("uvalueDesign.required"))
    );
  }

  @Test
  public void validate_form_valid() {
    var form = new DesignOpConditionsForm();
    form.setTemperatureOpMinMax(new MinMaxInput("1", "2"));
    form.setTemperatureDesignMinMax(new MinMaxInput("3", "4"));
    form.setPressureOpInternalExternal(new MinMaxInput("5", "6"));
    form.setPressureDesignInternalExternal(new MinMaxInput("7", "8"));
    form.setFlowrateOpMinMax(new MinMaxInput("9", "10"));
    form.setFlowrateDesignMinMax(new MinMaxInput("11", "12"));
    form.setUvalueOp("13");
    form.setUvalueDesign("14");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void validate_temperatureOp_invalid() {
    var form = createBlankForm();
    form.setTemperatureOpMinMax(new MinMaxInput("1.2", "2.2"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("temperatureOpMinMax.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode())),
        entry("temperatureOpMinMax.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode()))
    );
  }

  @Test
  public void validate_temperatureDesign_invalid() {
    var form = createBlankForm();
    form.setTemperatureDesignMinMax(new MinMaxInput("3.1", "4.1"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("temperatureDesignMinMax.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode())),
        entry("temperatureDesignMinMax.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode()))
    );
  }

  @Test
  public void validate_pressureOp_invalid() {
    var form = createBlankForm();
    form.setPressureOpInternalExternal(new MinMaxInput("-5.2", "-6.2"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("pressureOpInternalExternal.minValue",
            Set.of("minValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode(), "minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("pressureOpInternalExternal.maxValue",
            Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode(), "maxValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode()))
    );
    assertThat(errorsMap).doesNotContain(
        entry("pressureOpInternalExternal.minValue",
            Set.of("minValue" + MinMaxValidationErrorCodes.MIN_LARGER_THAN_MAX.getCode()))
    );
  }

  @Test
  public void validate_pressureDesign_invalid() {
    var form = createBlankForm();
    form.setPressureDesignInternalExternal(new MinMaxInput("-5.2", "-6.2"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("pressureDesignInternalExternal.minValue",
            Set.of("minValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode(), "minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("pressureDesignInternalExternal.maxValue",
            Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode(), "maxValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode()))
    );
    assertThat(errorsMap).doesNotContain(
        entry("pressureDesignInternalExternal.minValue",
            Set.of("minValue" + MinMaxValidationErrorCodes.MIN_LARGER_THAN_MAX.getCode()))
    );
  }

  @Test
  public void validate_flowrateOp_invalid() {
    var form = createBlankForm();
    form.setFlowrateOpMinMax(new MinMaxInput("-9", "5.333"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("flowrateOpMinMax.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("flowrateOpMinMax.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode()))
    );
  }

  @Test
  public void validate_flowrateDesign_invalid() {
    var form = createBlankForm();
    form.setFlowrateDesignMinMax(new MinMaxInput("-9", "5.333"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("flowrateDesignMinMax.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("flowrateDesignMinMax.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode()))
    );
  }

  @Test
  public void validate_uvalueOp_invalid() {
    var form = createBlankForm();
    form.setUvalueOp("-13.22");
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("uvalueOp", Set.of("uvalueOp" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode(),
            "uvalueOp" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode()))
    );
  }

  @Test
  public void validate_uvalueDesign_invalid() {
    var form = createBlankForm();
    form.setUvalueDesign("-13.22");
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("uvalueDesign", Set.of("uvalueDesign" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode(),
            "uvalueDesign" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode()))
    );
  }





}