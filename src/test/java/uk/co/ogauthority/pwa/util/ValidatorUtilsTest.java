package uk.co.ogauthority.pwa.util;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.BEFORE_TODAY;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.INVALID;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.model.form.appprocessing.applicationupdate.ApplicationUpdateRequestForm;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

public class ValidatorUtilsTest {

  private ProjectInformationForm projectInformationForm;

  private final String DATE_PICKER_FIELD_NAME = "deadlineTimestampStr";

  @Before
  public void setUp() {
    projectInformationForm = new ProjectInformationForm();
  }

  @Test
  public void validateDate_WithNulls() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDate("proposedStart", "proposed start", null, null, null, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactlyInAnyOrder(
            "proposedStartDay.required",
            "proposedStartMonth.required",
            "proposedStartYear.required"
        );
    assertThat(validationNoErrors).isFalse();
  }

  @Test
  public void validateDate_ValidDate() {
    var date = LocalDate.now();
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDate("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(ObjectError::getObjectName)
        .doesNotContain("proposedStartDay", "proposedStartMonth", "proposedStartYear");
    assertThat(validationNoErrors).isTrue();
  }

  @Test
  public void validateDate_yearHas1Digit() {
    BindingResult errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var isValid = ValidatorUtils.validateDate(
        "proposedStart", "proposed start",
        1, 1, 1,
        errors
    );

    var errorMap = ValidatorTestUtils.extractErrors(errors);

    assertThat(errorMap).contains(
        entry("proposedStartDay", Set.of(FieldValidationErrorCodes.INVALID.errorCode("proposedStartDay"))),
        entry("proposedStartMonth", Set.of(FieldValidationErrorCodes.INVALID.errorCode("proposedStartMonth"))),
        entry("proposedStartYear", Set.of(FieldValidationErrorCodes.INVALID.errorCode("proposedStartYear")))
    );

    assertThat(isValid).isFalse();
  }

  @Test
  public void validateDate_yearHas2DigitS() {
    BindingResult errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var isValid = ValidatorUtils.validateDate(
        "proposedStart", "proposed start",
        1, 1, 10,
        errors
    );

    var errorMap = ValidatorTestUtils.extractErrors(errors);

    assertThat(errorMap).contains(
        entry("proposedStartDay", Set.of(FieldValidationErrorCodes.INVALID.errorCode("proposedStartDay"))),
        entry("proposedStartMonth", Set.of(FieldValidationErrorCodes.INVALID.errorCode("proposedStartMonth"))),
        entry("proposedStartYear", Set.of(FieldValidationErrorCodes.INVALID.errorCode("proposedStartYear")))
    );

    assertThat(isValid).isFalse();
  }

  @Test
  public void validateDate_yearHas3DigitS() {
    BindingResult errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var isValid = ValidatorUtils.validateDate(
        "proposedStart", "proposed start",
        1, 1, 100,
        errors
    );

    var errorMap = ValidatorTestUtils.extractErrors(errors);

    assertThat(errorMap).contains(
        entry("proposedStartDay", Set.of(FieldValidationErrorCodes.INVALID.errorCode("proposedStartDay"))),
        entry("proposedStartMonth", Set.of(FieldValidationErrorCodes.INVALID.errorCode("proposedStartMonth"))),
        entry("proposedStartYear", Set.of(FieldValidationErrorCodes.INVALID.errorCode("proposedStartYear")))
    );

    assertThat(isValid).isFalse();
  }

  @Test
  public void validateDate_yearHas4DigitS() {
    BindingResult errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDate(
        "proposedStart", "proposed start",
        1, 1, 1000,
        errors
    );
    assertThat(errors.getAllErrors()).extracting(ObjectError::getObjectName)
        .doesNotContain("proposedStartDay", "proposedStartMonth", "proposedStartYear");
    assertThat(validationNoErrors).isTrue();
  }

  @Test
  public void validateDateIsPresentOrFuture_Past() {
    var date = LocalDate.now().minusDays(2);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDateIsPresentOrFuture("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactlyInAnyOrder(
            "proposedStartDay.beforeDate",
            "proposedStartMonth.beforeDate",
            "proposedStartYear.beforeDate"
        );
    assertThat(validationNoErrors).isFalse();
  }

  @Test
  public void validateDateIsPresentOrFuture_Future() {
    var date = LocalDate.now().plusDays(2);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDateIsPresentOrFuture("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(ObjectError::getObjectName)
        .doesNotContain("proposedStartDay", "proposedStartMonth", "proposedStartYear");
    assertThat(validationNoErrors).isTrue();
  }

  @Test
  public void validateDateIsPresentOrFuture_Present() {
    var date = LocalDate.now();
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDateIsPresentOrFuture("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(ObjectError::getObjectName)
        .doesNotContain("proposedStartDay", "proposedStartMonth", "proposedStartYear");
    assertThat(validationNoErrors).isTrue();
  }

  @Test
  public void validateDatePickerDateIsPresentOrFuture_null() {

    var form = new ApplicationUpdateRequestForm();
    Errors errors = new BeanPropertyBindingResult(form, "form");
    var isDateValid = ValidatorUtils.validateDatePickerDateIsPresentOrFuture(
        DATE_PICKER_FIELD_NAME, "deadline date", null, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactlyInAnyOrder(REQUIRED.errorCode(DATE_PICKER_FIELD_NAME));
    assertThat(isDateValid).isFalse();
  }



  @Test
  public void validateDatePickerDateIsPresentOrFuture_invalidDatePickerFormat() {

    var form = new ApplicationUpdateRequestForm();
    form.setDeadlineTimestampStr("12th Jan 2021");

    Errors errors = new BeanPropertyBindingResult(form, "form");
    var isDateValid = ValidatorUtils.validateDatePickerDateIsPresentOrFuture(
        DATE_PICKER_FIELD_NAME, "deadline date", form.getDeadlineTimestampStr(), errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsOnly(INVALID.errorCode(DATE_PICKER_FIELD_NAME));
    assertThat(isDateValid).isFalse();
  }

  @Test
  public void validateDatePickerDateIsPresentOrFuture_dateInThePast() {

    var form = new ApplicationUpdateRequestForm();
    form.setDeadlineTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now().minusDays(1)));

    Errors errors = new BeanPropertyBindingResult(form, "form");
    var isDateValid = ValidatorUtils.validateDatePickerDateIsPresentOrFuture(DATE_PICKER_FIELD_NAME, "deadline date",
        form.getDeadlineTimestampStr(), errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsOnly(BEFORE_TODAY.errorCode(DATE_PICKER_FIELD_NAME));
    assertThat(isDateValid).isFalse();
  }

  @Test
  public void validateDatePickerDateIsPresentOrFuture_dateIsToday() {

    var form = new ApplicationUpdateRequestForm();
    form.setDeadlineTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now()));

    Errors errors = new BeanPropertyBindingResult(form, "form");
    var isDateValid = ValidatorUtils.validateDatePickerDateIsPresentOrFuture(DATE_PICKER_FIELD_NAME, "deadline date",
        form.getDeadlineTimestampStr(), errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .doesNotContain(
            REQUIRED.errorCode(DATE_PICKER_FIELD_NAME),
            INVALID.errorCode(DATE_PICKER_FIELD_NAME),
            BEFORE_TODAY.errorCode(DATE_PICKER_FIELD_NAME)
        );
    assertThat(isDateValid).isTrue();
  }

  @Test
  public void validateDatePickerDateIsPresentOrFuture_dateIsInFuture() {

    var form = new ApplicationUpdateRequestForm();
    form.setDeadlineTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now().plusDays(1)));

    Errors errors = new BeanPropertyBindingResult(form, "form");
    var isDateValid = ValidatorUtils.validateDatePickerDateIsPresentOrFuture(DATE_PICKER_FIELD_NAME, "deadline date",
        form.getDeadlineTimestampStr(), errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .doesNotContain(
            REQUIRED.errorCode(DATE_PICKER_FIELD_NAME),
            INVALID.errorCode(DATE_PICKER_FIELD_NAME),
            BEFORE_TODAY.errorCode(DATE_PICKER_FIELD_NAME)
        );
    assertThat(isDateValid).isTrue();
  }



  @Test
  public void validateDateIsPastOrPresent_Past() {
    var date = LocalDate.now().minusDays(2);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDateIsPastOrPresent("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(ObjectError::getObjectName)
        .doesNotContain("proposedStartDay", "proposedStartMonth", "proposedStartYear");
    assertThat(validationNoErrors).isTrue();
  }

  @Test
  public void validateDateIsPastOrPresent_Future() {
    var date = LocalDate.now().plusDays(2);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDateIsPastOrPresent("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactlyInAnyOrder(
            "proposedStartDay.afterDate",
            "proposedStartMonth.afterDate",
            "proposedStartYear.afterDate"
        );
    assertThat(validationNoErrors).isFalse();
  }

  @Test
  public void validateDateIsPastOrPresent_Present() {
    var date = LocalDate.now();
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDateIsPastOrPresent("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(ObjectError::getObjectName)
        .doesNotContain("proposedStartDay", "proposedStartMonth", "proposedStartYear");
    assertThat(validationNoErrors).isTrue();
  }

  @Test
  public void isYearValid_validYear() {
    var isYearValid = ValidatorUtils.isYearValid(1000);
    assertThat(isYearValid).isTrue();
  }

  @Test
  public void isYearValid_yearTooBig() {
    var isYearValid = ValidatorUtils.isYearValid(4001);
    assertThat(isYearValid).isFalse();
  }


  @Test
  public void isYearValid_yearTooSmall() {
    var isYearValid = ValidatorUtils.isYearValid(999);
    assertThat(isYearValid).isFalse();
  }


  @Test
  public void validateDateWhenPresent_notPresent() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateDateWhenPresent("proposedStart", "proposed start",
        null, null, null, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .doesNotContain("proposedStartDay" +  FieldValidationErrorCodes.INVALID.getCode(),
            "proposedStarMonth" +  FieldValidationErrorCodes.INVALID.getCode(),
            "proposedStartYear" +  FieldValidationErrorCodes.INVALID.getCode());
  }

  @Test
  public void validateDateWhenPresent_validDate() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateDateWhenPresent("proposedStart", "proposed start",
        1, 1, 4000, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .doesNotContain("proposedStartDay" +  FieldValidationErrorCodes.INVALID.getCode(),
            "proposedStarMonth" +  FieldValidationErrorCodes.INVALID.getCode(),
            "proposedStartYear" +  FieldValidationErrorCodes.INVALID.getCode());
  }

  @Test
  public void validateDateWhenPresent_invalidDay_validMonthAndYear() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateDateWhenPresent("proposedStart", "proposed start",
        32, 12, 4000, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .contains("proposedStartDay" +  FieldValidationErrorCodes.INVALID.getCode());
  }

  @Test
  public void validateDateWhenPresent_invalidMonth_validDayAndYear() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateDateWhenPresent("proposedStart", "proposed start",
        1, 13, 4000, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .contains("proposedStartMonth" +  FieldValidationErrorCodes.INVALID.getCode());
  }

  @Test
  public void validateDateWhenPresent_invalidYear_validDayAndMonth() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateDateWhenPresent("proposedStart", "proposed start",
        1, 1, 4001, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .contains("proposedStartYear" +  FieldValidationErrorCodes.INVALID.getCode());
  }

  @Test
  public void validateDateWhenPresent_validFormat_invalidDayForMonth() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateDateWhenPresent("proposedStart", "proposed start",
        31, 2, 2020, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .contains("proposedStartDay" +  FieldValidationErrorCodes.INVALID.getCode());
  }

  @Test
  public void validateDateWhenPresent_validValues_dataPartiallyEntered() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateDateWhenPresent("proposedStart", "proposed start",
        null, 2, 2020, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .contains("proposedStartDay" +  FieldValidationErrorCodes.INVALID.getCode());
  }

  @Test
  public void validateDateWhenPresent_inValidValue_dataPartiallyEntered() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateDateWhenPresent("proposedStart", "proposed start",
        32, null, null, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .contains("proposedStartDay" +  FieldValidationErrorCodes.INVALID.getCode());
  }


  @Test
  public void validateBoolean_Null() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateBooleanTrue(errors, projectInformationForm.getUsingCampaignApproach(), "usingCampaignApproach", "Err");
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .contains("usingCampaignApproach.required");
  }

  @Test
  public void validateBoolean_False() {
    projectInformationForm.setUsingCampaignApproach(false);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateBooleanTrue(errors, projectInformationForm.getUsingCampaignApproach(),"usingCampaignApproach", "Err");
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .contains("usingCampaignApproach.required");
  }

  @Test
  public void validateBoolean_True() {
    projectInformationForm.setUsingCampaignApproach(true);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateBooleanTrue(errors, projectInformationForm.getUsingCampaignApproach(),"usingCampaignApproach", "Err");
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .doesNotContain("usingCampaignApproach.required");
  }

  @Test
  public void validateLatitude_valid() {

    var bindingResult = new BeanPropertyBindingResult(new CoordinateForm(), "form");
    ValidatorUtils.validateLatitude(
        bindingResult,
        "Start point",
        Pair.of("latitudeDegrees", 55),
        Pair.of("latitudeMinutes", 30),
        Pair.of("latitudeSeconds", BigDecimal.valueOf(44.44)));

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validateLatitude_invalid() {

    var bindingResult = new BeanPropertyBindingResult(new CoordinateForm(), "form");
    ValidatorUtils.validateLatitude(
        bindingResult,
        "Start point",
        Pair.of("latitudeDegrees", 44),
        Pair.of("latitudeMinutes", -1),
        Pair.of("latitudeSeconds", BigDecimal.valueOf(61)));

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("latitudeDegrees", Set.of("latitudeDegrees.invalid")),
        entry("latitudeMinutes", Set.of("latitudeMinutes.invalid")),
        entry("latitudeSeconds", Set.of("latitudeSeconds.invalid"))
    );

  }

  @Test
  public void validateLatitude_invalidSecondPrecision() {

    var bindingResult = new BeanPropertyBindingResult(new CoordinateForm(), "form");
    ValidatorUtils.validateLatitude(
        bindingResult,
        "Start point",
        Pair.of("latitudeDegrees", 55),
        Pair.of("latitideMinutes", 30),
        Pair.of("latitudeSeconds", BigDecimal.valueOf(45.555)));

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("latitudeSeconds", Set.of("latitudeSeconds.invalid"))
    );

  }

  @Test
  public void validateLongitude_valid() {

    var bindingResult = new BeanPropertyBindingResult(new CoordinateForm(), "form");
    ValidatorUtils.validateLongitude(
        bindingResult,
        "Start point",
        Pair.of("longitudeDegrees", 29),
        Pair.of("longitudeMinutes", 30),
        Pair.of("longitudeSeconds", BigDecimal.valueOf(44.44)),
        Pair.of("longitudeDirection", LongitudeDirection.EAST));

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validateLongitude_invalid() {

    var bindingResult = new BeanPropertyBindingResult(new CoordinateForm(), "form");
    ValidatorUtils.validateLongitude(
        bindingResult,
        "Start point",
        Pair.of("longitudeDegrees", 31),
        Pair.of("longitudeMinutes", 61),
        Pair.of("longitudeSeconds", BigDecimal.valueOf(-1)),
        Pair.of("longitudeDirection", LongitudeDirection.EAST));

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("longitudeDegrees", Set.of("longitudeDegrees.invalid")),
        entry("longitudeMinutes", Set.of("longitudeMinutes.invalid")),
        entry("longitudeSeconds", Set.of("longitudeSeconds.invalid"))
    );

  }

  @Test
  public void validateLongitude_invalidSecondPrecision() {

    var bindingResult = new BeanPropertyBindingResult(new CoordinateForm(), "form");
    ValidatorUtils.validateLongitude(
        bindingResult,
        "Start point",
        Pair.of("longitudeDegrees", 22),
        Pair.of("longitudeMinutes", 30),
        Pair.of("longitudeSeconds", BigDecimal.valueOf(33.333)),
        Pair.of("longitudeDirection", LongitudeDirection.WEST));

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("longitudeSeconds", Set.of("longitudeSeconds.invalid"))
    );

  }

  @Test
  public void validateDecimalPlaces_nullField() {
    var form = new PipelineIdentDataForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidatorUtils.validateDecimalPlaces(bindingResult, "externalDiameter", "External diameter", 2);
    assertThat(bindingResult.getAllErrors()).isEmpty();
  }

  @Test
  public void validateDecimalPlaces_invalidNumber() {
    var bindingResult = mock(BeanPropertyBindingResult.class);
    when(bindingResult.getFieldValue("externalDiameter")).thenReturn("12.34Xyz");
    ValidatorUtils.validateDecimalPlaces(bindingResult, "externalDiameter", "External diameter", 2);
    verify(bindingResult, never()).rejectValue(any(), any(), any());
  }



}