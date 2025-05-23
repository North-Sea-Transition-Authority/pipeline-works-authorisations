package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.MinMaxValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInputValidator;

@ExtendWith(MockitoExtension.class)
class PipelineOtherPropertiesDataValidatorTest {

  private PipelineOtherPropertiesDataValidator validator;

  private MinMaxInputValidator minMaxInputValidator;

  @BeforeEach
  void setUp() {
    minMaxInputValidator = new MinMaxInputValidator();
    validator = new PipelineOtherPropertiesDataValidator(minMaxInputValidator);
  }


  @Test
  void validate_availability_notSelected() {
    var form = new PipelineOtherPropertiesDataForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, OtherPipelineProperty.MERCURY,
      ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("propertyAvailabilityOption", Set.of("propertyAvailabilityOption.required"))
    );
  }


  private PipelineOtherPropertiesDataForm createForm(double min, double max) {
    var form = new PipelineOtherPropertiesDataForm();
    form.setPropertyAvailabilityOption(PropertyAvailabilityOption.AVAILABLE);
    form.setMinMaxInput(new MinMaxInput(String.valueOf(min), String.valueOf(max)));
    return form;
  }

  @Test
  void validate_invalid_waxContent() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(-3, 5.21), OtherPipelineProperty.WAX_CONTENT, ValidationType.FULL);

    assertThat(errorsMap).contains(
        entry("minMaxInput.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode()))
    );
  }

  @Test
  void validate_valid_waxContent() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(3, 5.2), OtherPipelineProperty.WAX_CONTENT, ValidationType.FULL);

    assertThat(errorsMap).doesNotContain(
        entry("minMaxInput.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode(),
            "minValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode())),
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode(),
            "maxValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode()))
    );
  }

  @Test
  void validate_invalid_waxAppearanceTemp() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(2.3, 5.2), OtherPipelineProperty.WAX_APPEARANCE_TEMPERATURE, ValidationType.FULL);

    assertThat(errorsMap).contains(
        entry("minMaxInput.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode())),
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode()))
    );
  }

  @Test
  void validate_valid_waxAppearanceTemp() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(2, 5), OtherPipelineProperty.WAX_APPEARANCE_TEMPERATURE, ValidationType.FULL);

    assertThat(errorsMap).doesNotContain(
        entry("minMaxInput.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode())),
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode()))
    );
  }

  @Test
  void validate_invalid_acidNum() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(-4, 5.2), OtherPipelineProperty.ACID_NUM, ValidationType.FULL);

    assertThat(errorsMap).contains(
        entry("minMaxInput.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode()))
    );
  }

  @Test
  void validate_invalid_viscosity() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(-4, 5.22), OtherPipelineProperty.VISCOSITY, ValidationType.FULL);

    assertThat(errorsMap).contains(
        entry("minMaxInput.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode()))
    );
  }

  @Test
  void validate_valid_viscosity() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(4, 5.2), OtherPipelineProperty.VISCOSITY, ValidationType.FULL);

    assertThat(errorsMap).doesNotContain(
        entry("minMaxInput.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode(),
            "minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode(),
            "maxValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode()))
    );
  }

  @Test
  void validate_invalid_densityGravity() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(-4, 5.22), OtherPipelineProperty.DENSITY_GRAVITY, ValidationType.FULL);

    assertThat(errorsMap).contains(
        entry("minMaxInput.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode()))
    );
  }


  @Test
  void validate_valid_densityGravity() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(4, 5), OtherPipelineProperty.DENSITY_GRAVITY, ValidationType.FULL);

    assertThat(errorsMap).doesNotContain(
        entry("minMaxInput.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode(),
            "maxValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode(),
            "maxValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode()))
    );
  }

  @Test
  void validate_invalid_sulphurContent() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(-4, 5.232), OtherPipelineProperty.SULPHUR_CONTENT, ValidationType.FULL);

    assertThat(errorsMap).contains(
        entry("minMaxInput.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode()))
    );
  }

  @Test
  void validate_invalid_pourPoint() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(4, 5.2), OtherPipelineProperty.POUR_POINT, ValidationType.FULL);

    assertThat(errorsMap).contains(
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode()))
    );
  }

  @Test
  void validate_invalid_solidContent() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(-4, 5.232), OtherPipelineProperty.SOLID_CONTENT, ValidationType.FULL);

    assertThat(errorsMap).contains(
        entry("minMaxInput.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode()))
    );
  }

  @Test
  void validate_invalid_mercury() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator,
        createForm(-4, 5.232), OtherPipelineProperty.MERCURY, ValidationType.FULL);

    assertThat(errorsMap).contains(
        entry("minMaxInput.minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        entry("minMaxInput.maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode()))
    );
  }

}
