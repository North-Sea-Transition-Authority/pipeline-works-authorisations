package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

class LocationDetailsSafetyZoneValidatorTest {

  private LocationDetailsSafetyZoneValidator validator;

  @BeforeEach
  void setUp() {
    validator = new LocationDetailsSafetyZoneValidator();
  }


  @Test
  void validate_noFacilities() {
    var form = new LocationDetailsSafetyZoneForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(result).contains(
        entry("facilities", Set.of("facilities" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_containsFacilities() {
    var form = new LocationDetailsSafetyZoneForm();
    form.setFacilities(List.of("1"));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(result).doesNotContainKeys("facilities");
  }


}