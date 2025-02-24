package uk.co.ogauthority.pwa.validators.publicnotice;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class FinalisePublicNoticeValidatorTest {

  private FinalisePublicNoticeValidator validator;


  @BeforeEach
  void setUp() {
    validator = new FinalisePublicNoticeValidator();
  }


  @Test
  void validate_form_empty() {
    var form = new FinalisePublicNoticeForm();
    form.setDaysToBePublishedFor(null);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, false);
    assertThat(errorsMap).containsOnly(
        entry("startDay", Set.of("startDay" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("startMonth", Set.of("startMonth" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("startYear", Set.of("startYear" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("daysToBePublishedFor", Set.of("daysToBePublishedFor" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_form_valid() {
    var form = new FinalisePublicNoticeForm();
    form.setStartDay(1);
    form.setStartMonth(1);
    form.setStartYear(2020);
    form.setDaysToBePublishedFor(28);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, false);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_changeFromPublish_reasonMissing() {
    var form = new FinalisePublicNoticeForm();
    form.setStartDay(1);
    form.setStartMonth(1);
    form.setStartYear(2020);
    form.setDaysToBePublishedFor(28);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, true);
    assertThat(errorsMap).hasSize(1);
    assertThat(errorsMap).containsOnly(entry("dateChangeReason", Set.of("dateChangeReason" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_changeFromPublish_valid() {
    var form = new FinalisePublicNoticeForm();
    form.setStartDay(1);
    form.setStartMonth(1);
    form.setStartYear(2020);
    form.setDaysToBePublishedFor(28);
    form.setDateChangeReason("Test");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, true);
    assertThat(errorsMap).isEmpty();
  }


}
