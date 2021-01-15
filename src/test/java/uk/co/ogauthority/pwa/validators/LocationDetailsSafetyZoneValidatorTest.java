package uk.co.ogauthority.pwa.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsSafetyZoneForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
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

  @Test
  public void validate_notificationSubmittedNull() {
    var form = new LocationDetailsSafetyZoneForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).contains(
        entry("psrNotificationSubmitted", Set.of("psrNotificationSubmitted" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_notificationSubmittedYes_submittedDateNotEntered() {
    var form = new LocationDetailsSafetyZoneForm();
    form.setPsrNotificationSubmitted(true);
    form.setPsrNotificationSubmittedDate(new TwoFieldDateInput());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).contains(
        entry("psrNotificationSubmittedDate.month", Set.of("month" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("psrNotificationSubmittedDate.year", Set.of("year" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_notificationSubmittedYes_submittedDateAfterToday() {
    var form = new LocationDetailsSafetyZoneForm();
    form.setPsrNotificationSubmitted(true);
    form.setPsrNotificationSubmittedDate(new TwoFieldDateInput(LocalDate.now().getYear() + 1, 1));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).contains(
        entry("psrNotificationSubmittedDate.month", Set.of("month" + FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode())),
        entry("psrNotificationSubmittedDate.year", Set.of("year" + FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode())));
  }

  @Test
  public void validate_notificationSubmittedNo_expectedSubmissionDateNotEntered() {
    var form = new LocationDetailsSafetyZoneForm();
    form.setPsrNotificationSubmitted(false);
    form.setPsrNotificationExpectedSubmissionDate(new TwoFieldDateInput());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).contains(
        entry("psrNotificationExpectedSubmissionDate.month", Set.of("month" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("psrNotificationExpectedSubmissionDate.year", Set.of("year" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_notificationSubmittedNo_expectedSubmissionDateBeforeToday() {
    var form = new LocationDetailsSafetyZoneForm();
    form.setPsrNotificationSubmitted(false);
    form.setPsrNotificationExpectedSubmissionDate(new TwoFieldDateInput(LocalDate.now().getYear() - 1, 1));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).contains(
        entry("psrNotificationExpectedSubmissionDate.month", Set.of("month" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())),
        entry("psrNotificationExpectedSubmissionDate.year", Set.of("year" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));
  }



}