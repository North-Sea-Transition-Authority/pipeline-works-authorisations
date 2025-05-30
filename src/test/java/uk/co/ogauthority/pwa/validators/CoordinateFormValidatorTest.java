package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeDirection;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;
import uk.co.ogauthority.pwa.model.form.enums.ValueRequirement;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class CoordinateFormValidatorTest {

  private CoordinateFormValidator validator;

  @BeforeEach
  void setUp() {
    validator = new CoordinateFormValidator();
  }

  @Test
  void valid_mandatory_dataPresent() {

    var form = buildForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, null, ValueRequirement.MANDATORY, "Start point");

    assertThat(result).isEmpty();

  }

  @Test
  void failed_mandatory_secondsUnder2Dp() {

    var form = buildForm();
    form.setLongitudeSeconds(BigDecimal.valueOf(1.1));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, null, ValueRequirement.MANDATORY, "Start point");

    assertThat(result).containsOnly(
        entry("longitudeSeconds", Set.of("longitudeSeconds.invalid"))
    );

  }

  @Test
  void failed_mandatory_secondsOver2Dp() {

    var form = buildForm();
    form.setLongitudeSeconds(BigDecimal.valueOf(1.111));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, null, ValueRequirement.MANDATORY, "Start point");

    assertThat(result).containsOnly(
        entry("longitudeSeconds", Set.of("longitudeSeconds.invalid"))
    );

  }

  @Test
  void failed_mandatory_dataNotPresent() {

    var form = new CoordinateForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, null, ValueRequirement.MANDATORY, "Start point");

    assertThat(result).containsOnly(
        entry("latitudeDegrees", Set.of("latitudeDegrees.required")),
        entry("latitudeMinutes", Set.of("latitudeMinutes.required")),
        entry("latitudeSeconds", Set.of("latitudeSeconds.required")),
        entry("longitudeDegrees", Set.of("longitudeDegrees.required")),
        entry("longitudeMinutes", Set.of("longitudeMinutes.required")),
        entry("longitudeSeconds", Set.of("longitudeSeconds.required")),
        entry("longitudeDirection", Set.of("longitudeDirection.required"))
    );

  }

  @Test
  void valid_optional_dataNotPresent() {

    var form = new CoordinateForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, null, ValueRequirement.OPTIONAL, "Start point");

    assertThat(result).isEmpty();

  }

  @Test
  void invalid_optional_partialDataPresent() {

    var form = new CoordinateForm();
    form.setLatitudeDegrees(50);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, null, ValueRequirement.OPTIONAL, "Start point");

    assertThat(result).containsOnly(
        entry("latitudeDegrees", Set.of("latitudeDegrees.required")),
        entry("latitudeMinutes", Set.of("latitudeMinutes.required")),
        entry("latitudeSeconds", Set.of("latitudeSeconds.required")),
        entry("longitudeDegrees", Set.of("longitudeDegrees.required")),
        entry("longitudeMinutes", Set.of("longitudeMinutes.required")),
        entry("longitudeSeconds", Set.of("longitudeSeconds.required")),
        entry("longitudeDirection", Set.of("longitudeDirection.required"))
    );

  }

  private CoordinateForm buildForm() {

    var form = new CoordinateForm();

    form.setLatitudeDegrees(50);
    form.setLatitudeMinutes(40);
    form.setLatitudeSeconds(BigDecimal.valueOf(30.33));
    form.setLatitudeDirection(LatitudeDirection.NORTH);

    form.setLongitudeDegrees(12);
    form.setLongitudeMinutes(13);
    form.setLongitudeSeconds(new BigDecimal("14.00"));
    form.setLongitudeDirection(LongitudeDirection.EAST);

    return form;

  }


}
