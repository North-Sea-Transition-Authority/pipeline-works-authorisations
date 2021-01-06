package uk.co.ogauthority.pwa.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsSafetyZoneForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

public class LocationDetailsSafetyZoneValidatorTest {

  private LocationDetailsSafetyZoneValidator safetyZoneValidator;

  @Before
  public void setUp() {
    safetyZoneValidator = new LocationDetailsSafetyZoneValidator();
  }


  @Test
  public void validate_withinSafetyZone_no() {
    var form = new LocationDetailsSafetyZoneForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    var result = ValidatorTestUtils.getFormValidationErrors(safetyZoneValidator, form);
    assertThat(result).doesNotContainKeys("withinSafetyZone");
  }

  @Test
  public void validate_withinSafetyZone_partially_nullFacilities() {
    var form = new LocationDetailsSafetyZoneForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    var result = ValidatorTestUtils.getFormValidationErrors(safetyZoneValidator, form);
    assertThat(result).doesNotContainKeys("withinSafetyZone");
    assertThat(result).contains(
        entry("facilitiesIfPartially", Set.of("facilitiesIfPartially" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_withinSafetyZone_partially_withFacilities() {
    var form = new LocationDetailsSafetyZoneForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    form.setFacilitiesIfPartially(List.of("1"));
    var result = ValidatorTestUtils.getFormValidationErrors(safetyZoneValidator, form);
    assertThat(result).doesNotContainKeys("withinSafetyZone", "facilitiesIfPartially");
  }

  @Test
  public void validate_withinSafetyZone_yes_nullFacilities() {
    var form = new LocationDetailsSafetyZoneForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    var result = ValidatorTestUtils.getFormValidationErrors(safetyZoneValidator, form);
    assertThat(result).doesNotContainKeys("withinSafetyZone");
    assertThat(result).contains(
        entry("facilitiesIfYes", Set.of("facilitiesIfYes" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_withinSafetyZone_yes_withFacilities() {
    var form = new LocationDetailsSafetyZoneForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    form.setFacilitiesIfYes(List.of("1"));
    var result = ValidatorTestUtils.getFormValidationErrors(safetyZoneValidator, form);
    assertThat(result).doesNotContainKeys("withinSafetyZone", "facilitiesIfYes");
  }



}