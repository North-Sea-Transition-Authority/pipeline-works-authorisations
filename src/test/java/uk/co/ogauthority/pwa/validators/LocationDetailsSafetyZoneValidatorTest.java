package uk.co.ogauthority.pwa.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsSafetyZoneForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

public class LocationDetailsSafetyZoneValidatorTest {

  private LocationDetailsSafetyZoneValidator validator;
  private TwoFieldDateInputValidator twoFieldDateInputValidator;

  @Before
  public void setUp() {
    twoFieldDateInputValidator = new TwoFieldDateInputValidator();
    validator = new LocationDetailsSafetyZoneValidator(twoFieldDateInputValidator);
  }


  @Test
  public void validate_noFacilities() {
    var form = new LocationDetailsSafetyZoneForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).contains(
        entry("facilities", Set.of("facilities" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_containsFacilities() {
    var form = new LocationDetailsSafetyZoneForm();
    form.setFacilities(List.of("1"));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).doesNotContainKeys("facilities");
  }



}