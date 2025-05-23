package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

class LocationDetailsValidatorTest {

  private LocationDetailsValidator locationDetailsValidator;

  private static int INVALID_LARGE_YEAR = 4001;
  private static int INVALID_SMALL_YEAR = 999;


  @BeforeEach
  void setUp() {
    TwoFieldDateInputValidator twoFieldDateInputValidator = new TwoFieldDateInputValidator();
    LocationDetailsSafetyZoneValidator safetyZoneValidator = new LocationDetailsSafetyZoneValidator();
    locationDetailsValidator = new LocationDetailsValidator(safetyZoneValidator, twoFieldDateInputValidator);
  }


  private LocationDetailsFormValidationHints getValidationHintsFull(Set<LocationDetailsQuestion> requiredQuestions) {
    return new LocationDetailsFormValidationHints(
        ValidationType.FULL, requiredQuestions);
  }

  private LocationDetailsFormValidationHints getValidationHintsPartial(Set<LocationDetailsQuestion> requiredQuestions) {
    return new LocationDetailsFormValidationHints(
        ValidationType.PARTIAL, requiredQuestions);
  }

  @Test
  void validate_full_allQuestionsRequired_formAllNulls() {
    var form = new LocationDetailsForm();
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(EnumSet.allOf(LocationDetailsQuestion.class)));
    assertThat(result).containsOnly(
        entry("withinSafetyZone", Set.of("withinSafetyZone" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("psrNotificationSubmittedOption", Set.of("psrNotificationSubmittedOption" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("diversUsed", Set.of("diversUsed" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("transportsMaterialsToShore",
            Set.of("transportsMaterialsToShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("transportsMaterialsFromShore",
            Set.of("transportsMaterialsFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("uploadedFiles", Set.of(FileValidationUtils.BELOW_THRESHOLD_ERROR_CODE))
    );
  }

  @Test
  void validate_partial_allQuestionsRequired_formAllNulls() {
    var form = new LocationDetailsForm();
    var errors = new BeanPropertyBindingResult(form, "form");
    locationDetailsValidator.validate(form, errors,
        getValidationHintsPartial(EnumSet.allOf(LocationDetailsQuestion.class)));
    var result = errors.getFieldErrors().stream()
        .collect(
            Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
    assertThat(result).isEmpty();
  }

  @Test
  void validate_full_formAllEmptyText() {
    var form = new LocationDetailsForm();
    form.setApproximateProjectLocationFromShore("");
    form.setTransportationMethodToShore("");
    form.setPipelineRouteDetails("");
    form.setTransportsMaterialsToShore(true);
    form.setTransportsMaterialsFromShore(true);
    form.setPsrNotificationSubmittedOption(PsrNotification.NOT_REQUIRED);
    form.setPsrNotificationNotRequiredReason("");
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(EnumSet.allOf(LocationDetailsQuestion.class)));
    assertThat(result).containsOnly(
        entry("withinSafetyZone", Set.of("withinSafetyZone" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("psrNotificationNotRequiredReason", Set.of("psrNotificationNotRequiredReason" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("diversUsed", Set.of("diversUsed" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("transportationMethodToShore", Set.of("transportationMethodToShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("transportationMethodFromShore", Set.of("transportationMethodFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("uploadedFiles", Set.of(FileValidationUtils.BELOW_THRESHOLD_ERROR_CODE))
    );
  }

  @Test
  void validate_full_withinSafetyZone_no() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)));
    assertThat(result).doesNotContainKeys("withinSafetyZone");
  }

  @Test
  void validate_partial_withinSafetyZone_no() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsPartial(Set.of(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)));
    assertThat(result).doesNotContainKeys("withinSafetyZone");
  }

  @Test
  void validate_full_withinSafetyZone_partially_nullFacilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)));
    assertThat(result).doesNotContainKeys("withinSafetyZone");
    assertThat(result).contains(
        entry("partiallyWithinSafetyZoneForm.facilities", Set.of("facilities" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_partial_withinSafetyZone_partially_nullFacilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsPartial(Set.of(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)));
    assertThat(result).isEmpty();
  }

  @Test
  void validate_full_withinSafetyZone_partially_withFacilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    form.getPartiallyWithinSafetyZoneForm().setFacilities(List.of("1"));
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)));
    assertThat(result).doesNotContainKeys("withinSafetyZone", "partiallyWithinSafetyZoneForm.facilities");
  }

  @Test
  void validate_partial_withinSafetyZone_partially_withFacilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    form.getPartiallyWithinSafetyZoneForm().setFacilities(List.of("1"));
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsPartial(Set.of(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)));
    assertThat(result).isEmpty();
  }

  @Test
  void validate_full_withinSafetyZone_yes_nullFacilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)));
    assertThat(result).doesNotContainKeys("withinSafetyZone");
    assertThat(result).contains(
        entry("completelyWithinSafetyZoneForm.facilities", Set.of("facilities" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_partial_withinSafetyZone_yes_nullFacilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsPartial(Set.of(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)));
    assertThat(result).isEmpty();
  }

  @Test
  void validate_full_withinSafetyZone_yes_withFacilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    form.getCompletelyWithinSafetyZoneForm().setFacilities(List.of("1"));
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)));
    assertThat(result).doesNotContainKeys("withinSafetyZone", "completelyWithinSafetyZoneForm.facilities");
  }

  @Test
  void validate_partial_withinSafetyZone_yes_withFacilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    form.getCompletelyWithinSafetyZoneForm().setFacilities(List.of("1"));
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsPartial(Set.of(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)));
    assertThat(result).isEmpty();
  }

  @Test
  void validate_full_surveyConcludedDate_nulls() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).contains(
        entry("surveyConcludedDay", Set.of("surveyConcludedDay" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("surveyConcludedMonth", Set.of("surveyConcludedMonth" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("surveyConcludedYear", Set.of("surveyConcludedYear" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_full_surveyConcludedDate_yearTooBig() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    form.setSurveyConcludedYear(4001);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).contains(
        entry("surveyConcludedYear", Set.of("surveyConcludedYear" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_full_surveyConcludedDate_yearTooSmall() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    form.setSurveyConcludedYear(999);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).contains(
        entry("surveyConcludedYear", Set.of("surveyConcludedYear" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_full_surveyConcludedDate_valid() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    form.setSurveyConcludedDay(1);
    form.setSurveyConcludedMonth(1);
    form.setSurveyConcludedYear(2020);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).doesNotContainKeys("surveyConcludedDay", "surveyConcludedMonth", "surveyConcludedYear");
  }

  @Test
  void validate_full_pipelineRouteDetails_noText_routeUndertaken() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).containsKeys("pipelineRouteDetails");
  }

  @Test
  void validate_full_pipelineRouteDetails_withText_routeUndertaken() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    form.setPipelineRouteDetails("route");
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).doesNotContainKeys("pipelineRouteDetails");
  }

  @Test
  void validate_full_pipelineRouteDetails_noText_routeNotUndertaken() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(false);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).doesNotContainKeys("pipelineRouteDetails");
  }

  @Test
  void validate_partial_pipelineRouteDetailsText_tooLong() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    form.setPipelineRouteDetails(ValidatorTestUtils.overMaxDefaultCharLength());
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsPartial(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).contains(
        Map.entry("pipelineRouteDetails", Set.of("pipelineRouteDetails" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

  @Test
  void validate_full_routeNotUndertakenReason_null() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(false);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).contains(
        Map.entry("routeSurveyNotUndertakenReason", Set.of("routeSurveyNotUndertakenReason" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_partial_routeNotUndertakenReason_tooLong() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(false);
    form.setRouteSurveyNotUndertakenReason(ValidatorTestUtils.overMaxDefaultCharLength());
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsPartial(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).contains(
        Map.entry("routeSurveyNotUndertakenReason", Set.of("routeSurveyNotUndertakenReason" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

  @Test
  void validate_full_valid() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    form.setPsrNotificationSubmittedOption(PsrNotification.YES);
    form.setPsrNotificationSubmittedDate(new TwoFieldDateInput(2021, 6));
    form.setDiversUsed(true);
    form.setTransportsMaterialsToShore(true);
    form.setTransportsMaterialsFromShore(true);
    form.setApproximateProjectLocationFromShore("Approx");
    form.setTransportationMethodToShore("Method");
    form.setTransportationMethodFromShore("Test");
    form.setFacilitiesOffshore(true);
    form.setRouteSurveyUndertaken(false);
    form.setRouteSurveyNotUndertakenReason("reason");
    form.setPipelineRouteDetails("Detail text");
    form.setWithinLimitsOfDeviation(true);
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(EnumSet.allOf(LocationDetailsQuestion.class)));
    assertThat(result).isEmpty();
  }

  @Test
  void validate_partial_tooManyCharacters() {
    var form = new LocationDetailsForm();
    form.setApproximateProjectLocationFromShore(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setTransportationMethodToShore(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setTransportationMethodFromShore(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setPipelineAshoreLocation(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setPsrNotificationSubmittedOption(PsrNotification.NOT_REQUIRED);
    form.setPsrNotificationNotRequiredReason(ValidatorTestUtils.overMaxDefaultCharLength());


    var errors = new BeanPropertyBindingResult(form, "form");
    locationDetailsValidator.validate(form, errors,
        getValidationHintsPartial(Set.of(
            LocationDetailsQuestion.APPROXIMATE_PROJECT_LOCATION_FROM_SHORE,
            LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE,
            LocationDetailsQuestion.TRANSPORTS_MATERIALS_FROM_SHORE,
            LocationDetailsQuestion.FACILITIES_OFFSHORE,
            LocationDetailsQuestion.PSR_NOTIFICATION
        )));

    var result = errors.getFieldErrors().stream()
        .collect(
            Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
    assertThat(result).contains(
        entry("approximateProjectLocationFromShore", Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())),
        entry("transportationMethodToShore", Set.of("transportationMethodToShore" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())),
        entry("transportationMethodFromShore", Set.of("transportationMethodFromShore" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())),
        entry("pipelineAshoreLocation", Set.of("pipelineAshoreLocation" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())),
        entry("psrNotificationNotRequiredReason", Set.of("psrNotificationNotRequiredReason" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }

  @Test
  void validate_partial_invalidDate() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    form.setSurveyConcludedDay(1);

    var errors = new BeanPropertyBindingResult(form, "form");
    locationDetailsValidator.validate(form, errors,
        getValidationHintsPartial(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    var result = errors.getFieldErrors().stream()
        .collect(
            Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
    assertThat(result).contains(
        entry("surveyConcludedDay", Set.of("surveyConcludedDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("surveyConcludedMonth", Set.of("surveyConcludedMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("surveyConcludedYear", Set.of("surveyConcludedYear" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_partial_valid() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    form.setSurveyConcludedDay(1);
    form.setSurveyConcludedMonth(1);
    form.setSurveyConcludedYear(2020);

    var errors = new BeanPropertyBindingResult(form, "form");
    locationDetailsValidator.validate(form, errors,
        getValidationHintsPartial(EnumSet.allOf(LocationDetailsQuestion.class)));
    var result = errors.getFieldErrors().stream()
        .collect(
            Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
    assertThat(result).isEmpty();
  }

  @Test
  void validate_full_facilitiesOffshore_false_noText() {
    var form = new LocationDetailsForm();
    form.setFacilitiesOffshore(false);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.FACILITIES_OFFSHORE)));
    assertThat(result).containsKeys("pipelineAshoreLocation");
  }

  @Test
  void validate_full_facilitiesOffshore_false_withText() {
    var form = new LocationDetailsForm();
    form.setFacilitiesOffshore(false);
    form.setPipelineAshoreLocation("Test");
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.FACILITIES_OFFSHORE)));
    assertThat(result).doesNotContainKeys("pipelineAshoreLocation");
  }

  @Test
  void validate_full_transportMethod_true_noText() {
    var form = new LocationDetailsForm();
    form.setTransportsMaterialsToShore(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE)));
    assertThat(result).containsKeys("transportationMethodToShore");
    assertThat(result).doesNotContainKeys("transportsMaterialsToShore");
  }

  @Test
  void validate_full_transportMethodToShore_false() {
    var form = new LocationDetailsForm();
    form.setTransportsMaterialsToShore(false);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE)));
    assertThat(result).doesNotContainKeys("transportsMaterialsToShore");
  }

  @Test
  void validate_full_transportMethodToShore_true_withText() {
    var form = new LocationDetailsForm();
    form.setTransportsMaterialsToShore(true);
    form.setTransportationMethodToShore("Test");
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE)));
    assertThat(result).doesNotContainKeys("transportationMethod", "transportsMaterialsToShore");
  }

  @Test
  void validate_full_transportMethodFromShore_false() {
    var form = new LocationDetailsForm();
    form.setTransportsMaterialsFromShore(false);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.TRANSPORTS_MATERIALS_FROM_SHORE)));
    assertThat(result).doesNotContainKeys("transportsMaterialsFromShore");
  }

  @Test
  void validate_full_transportMethodFromShore_true_withText() {
    var form = new LocationDetailsForm();
    form.setTransportsMaterialsFromShore(true);
    form.setTransportationMethodFromShore("Test");
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.TRANSPORTS_MATERIALS_FROM_SHORE)));
    assertThat(result).doesNotContainKeys("transportationMethod", "transportsMaterialsFromShore");
  }

  @Test
  void validate_notificationSubmittedYes_submittedDateNotEntered() {
    var form = new LocationDetailsForm();
    form.setPsrNotificationSubmittedOption(PsrNotification.YES);
    form.setPsrNotificationSubmittedDate(new TwoFieldDateInput());
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHintsFull(Set.of(LocationDetailsQuestion.PSR_NOTIFICATION)));
    assertThat(result).contains(
        entry("psrNotificationSubmittedDate.month", Set.of("month" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("psrNotificationSubmittedDate.year", Set.of("year" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_notificationSubmittedYes_submittedDateAfterToday() {
    var form = new LocationDetailsForm();
    form.setPsrNotificationSubmittedOption(PsrNotification.YES);
    form.setPsrNotificationSubmittedDate(new TwoFieldDateInput(LocalDate.now().getYear() + 1, 1));
    var result = ValidatorTestUtils.getFormValidationErrors(
        locationDetailsValidator, form, getValidationHintsFull(Set.of(LocationDetailsQuestion.PSR_NOTIFICATION)));
    assertThat(result).contains(
        entry("psrNotificationSubmittedDate.month", Set.of("month" + FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode())),
        entry("psrNotificationSubmittedDate.year", Set.of("year" + FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode())));
  }

  @Test
  void validate_notificationSubmittedNo_expectedSubmissionDateNotEntered() {
    var form = new LocationDetailsForm();
    form.setPsrNotificationSubmittedOption(PsrNotification.NO);
    form.setPsrNotificationExpectedSubmissionDate(new TwoFieldDateInput());
    var result = ValidatorTestUtils.getFormValidationErrors(
        locationDetailsValidator, form, getValidationHintsFull(Set.of(LocationDetailsQuestion.PSR_NOTIFICATION)));
    assertThat(result).contains(
        entry("psrNotificationExpectedSubmissionDate.month", Set.of("month" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("psrNotificationExpectedSubmissionDate.year", Set.of("year" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_notificationSubmittedNo_expectedSubmissionDateBeforeToday() {
    var form = new LocationDetailsForm();
    form.setPsrNotificationSubmittedOption(PsrNotification.NO);
    form.setPsrNotificationExpectedSubmissionDate(new TwoFieldDateInput(LocalDate.now().getYear() - 1, 1));
    var result = ValidatorTestUtils.getFormValidationErrors(
        locationDetailsValidator, form, getValidationHintsFull(Set.of(LocationDetailsQuestion.PSR_NOTIFICATION)));
    assertThat(result).contains(
        entry("psrNotificationExpectedSubmissionDate.month", Set.of("month" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())),
        entry("psrNotificationExpectedSubmissionDate.year", Set.of("year" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));
  }

  @Test
  void validate_notificationSubmittedYes_yearTooBig() {
    var form = new LocationDetailsForm();
    form.setPsrNotificationSubmittedOption(PsrNotification.YES);
    form.setPsrNotificationSubmittedDate(new TwoFieldDateInput(INVALID_LARGE_YEAR, 1));
    var result = ValidatorTestUtils.getFormValidationErrors(
        locationDetailsValidator, form, getValidationHintsFull(Set.of(LocationDetailsQuestion.PSR_NOTIFICATION)));
    assertThat(result).contains(
        entry("psrNotificationSubmittedDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  void validate_notificationSubmittedYes_yearTooSmall() {
    var form = new LocationDetailsForm();
    form.setPsrNotificationSubmittedOption(PsrNotification.YES);
    form.setPsrNotificationSubmittedDate(new TwoFieldDateInput(INVALID_SMALL_YEAR, 1));
    var result = ValidatorTestUtils.getFormValidationErrors(
        locationDetailsValidator, form, getValidationHintsFull(Set.of(LocationDetailsQuestion.PSR_NOTIFICATION)));
    assertThat(result).contains(
        entry("psrNotificationSubmittedDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  void validate_notificationSubmittedNo_yearTooBig() {
    var form = new LocationDetailsForm();
    form.setPsrNotificationSubmittedOption(PsrNotification.NO);
    form.setPsrNotificationExpectedSubmissionDate(new TwoFieldDateInput(INVALID_LARGE_YEAR, 1));
    var result = ValidatorTestUtils.getFormValidationErrors(
        locationDetailsValidator, form, getValidationHintsFull(Set.of(LocationDetailsQuestion.PSR_NOTIFICATION)));
    assertThat(result).contains(
        entry("psrNotificationExpectedSubmissionDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  void validate_notificationSubmittedNo_yearTooSmall() {
    var form = new LocationDetailsForm();
    form.setPsrNotificationSubmittedOption(PsrNotification.NO);
    form.setPsrNotificationExpectedSubmissionDate(new TwoFieldDateInput(INVALID_SMALL_YEAR, 1));
    var result = ValidatorTestUtils.getFormValidationErrors(
        locationDetailsValidator, form, getValidationHintsFull(Set.of(LocationDetailsQuestion.PSR_NOTIFICATION)));
    assertThat(result).contains(
        entry("psrNotificationExpectedSubmissionDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  void validate_full_routeDocumentUploaded_hasDescription_valid() {

    var form = new LocationDetailsForm();
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form, getValidationHintsFull(Set.of()));
    assertThat(errorsMap).doesNotContain(
        Map.entry("uploadedFileWithDescriptionForms[0].uploadedFileDescription",
            Set.of("uploadedFileWithDescriptionForms[0].uploadedFileDescription" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_full_routeDocumentUploaded_noDescription_invalid() {

    var form = new LocationDetailsForm();
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form, getValidationHintsFull(Set.of()));
    assertThat(errorsMap).contains(
        Map.entry("uploadedFiles[0].uploadedFileDescription",
            Set.of("uploadedFiles[0].uploadedFileDescription" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_partial_routeDocumentUploaded_noDescription_valid() {

    var form = new LocationDetailsForm();
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form, getValidationHintsPartial(Set.of()));
    assertThat(errorsMap).doesNotContain(
        Map.entry("uploadedFileWithDescriptionForms[0].uploadedFileDescription",
            Set.of("uploadedFileWithDescriptionForms[0].uploadedFileDescription" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

}
