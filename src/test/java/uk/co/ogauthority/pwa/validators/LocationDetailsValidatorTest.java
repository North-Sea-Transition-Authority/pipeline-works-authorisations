package uk.co.ogauthority.pwa.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.enums.LocationDetailsQuestion;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

public class LocationDetailsValidatorTest {

  private LocationDetailsValidator locationDetailsValidator;
  private LocationDetailsSafetyZoneValidator safetyZoneValidator;

  @Before
  public void setUp() {
    safetyZoneValidator = new LocationDetailsSafetyZoneValidator();
    locationDetailsValidator = new LocationDetailsValidator(safetyZoneValidator);
  }


  private LocationDetailsFormValidationHints getValidationHints(Set<LocationDetailsQuestion> requiredQuestions) {
    return new LocationDetailsFormValidationHints(
        ValidationType.FULL, requiredQuestions);
  }

  private LocationDetailsFormValidationHints getValidationHintsPartial(Set<LocationDetailsQuestion> requiredQuestions) {
    return new LocationDetailsFormValidationHints(
        ValidationType.PARTIAL, requiredQuestions);
  }

  @Test
  public void validate_nulls() {
    var form = new LocationDetailsForm();
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(EnumSet.allOf(LocationDetailsQuestion.class)));
    assertThat(result).containsOnly(
        entry("safetyZoneQuestionForm.withinSafetyZone", Set.of("withinSafetyZone" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("transportsMaterialsToShore",
            Set.of("transportsMaterialsToShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_emptyText() {
    var form = new LocationDetailsForm();
    form.setApproximateProjectLocationFromShore("");
    form.setTransportationMethod("");
    form.setPipelineRouteDetails("");
    form.setTransportsMaterialsToShore(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(EnumSet.allOf(LocationDetailsQuestion.class)));
    assertThat(result).containsOnly(
        entry("safetyZoneQuestionForm.withinSafetyZone", Set.of("withinSafetyZone" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("transportationMethod", Set.of("transportationMethod" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_surveyConcludedDate_nulls() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).contains(
        entry("surveyConcludedDay", Set.of("surveyConcludedDay" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("surveyConcludedMonth", Set.of("surveyConcludedMonth" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("surveyConcludedYear", Set.of("surveyConcludedYear" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_surveyConcludedDate_valid() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    form.setSurveyConcludedDay(1);
    form.setSurveyConcludedMonth(1);
    form.setSurveyConcludedYear(2020);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).doesNotContainKeys("surveyConcludedDay", "surveyConcludedMonth", "surveyConcludedYear");
  }

  @Test
  public void validate_pipelineRouteDetails_noText_routeUndertaken() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).containsKeys("pipelineRouteDetails");
  }

  @Test
  public void validate_pipelineRouteDetails_withText_routeUndertaken() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    form.setPipelineRouteDetails("route");
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).doesNotContainKeys("pipelineRouteDetails");
  }

  @Test
  public void validate_pipelineRouteDetails_noText_routeNotUndertaken() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(false);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(Set.of(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)));
    assertThat(result).doesNotContainKeys("pipelineRouteDetails");
  }

  @Test
  public void validate_valid() {
    var form = new LocationDetailsForm();
    form.getSafetyZoneQuestionForm().setWithinSafetyZone(HseSafetyZone.NO);
    form.setTransportsMaterialsToShore(true);
    form.setApproximateProjectLocationFromShore("Approx");
    form.setTransportationMethod("Method");
    form.setFacilitiesOffshore(true);
    form.setRouteSurveyUndertaken(false);
    form.setPipelineRouteDetails("Detail text");
    form.setWithinLimitsOfDeviation(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(EnumSet.allOf(LocationDetailsQuestion.class)));
    assertThat(result).isEmpty();
  }

  @Test
  public void validatePartial_nulls() {
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
  public void validatePartial_tooManyCharacters() {
    var form = new LocationDetailsForm();
    form.setApproximateProjectLocationFromShore(ValidatorTestUtils.over4000Chars());
    form.setTransportationMethod(ValidatorTestUtils.over4000Chars());
    form.setPipelineAshoreLocation(ValidatorTestUtils.over4000Chars());

    var errors = new BeanPropertyBindingResult(form, "form");
    locationDetailsValidator.validate(form, errors,
        getValidationHintsPartial(Set.of(
            LocationDetailsQuestion.APPROXIMATE_PROJECT_LOCATION_FROM_SHORE,
            LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE,
            LocationDetailsQuestion.FACILITIES_OFFSHORE
        )));

    var result = errors.getFieldErrors().stream()
        .collect(
            Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
    assertThat(result).contains(
        entry("approximateProjectLocationFromShore", Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())),
        entry("transportationMethod", Set.of("transportationMethod" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())),
        entry("pipelineAshoreLocation", Set.of("pipelineAshoreLocation" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }

  @Test
  public void validatePartial_invalidDate() {
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
  public void validatePartial_valid() {
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
  public void validate_facilitiesOffshore_false_noText() {
    var form = new LocationDetailsForm();
    form.setFacilitiesOffshore(false);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(Set.of(LocationDetailsQuestion.FACILITIES_OFFSHORE)));
    assertThat(result).containsKeys("pipelineAshoreLocation");
  }

  @Test
  public void validate_facilitiesOffshore_false_withText() {
    var form = new LocationDetailsForm();
    form.setFacilitiesOffshore(false);
    form.setPipelineAshoreLocation("Test");
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(Set.of(LocationDetailsQuestion.FACILITIES_OFFSHORE)));
    assertThat(result).doesNotContainKeys("pipelineAshoreLocation");
  }

  @Test
  public void validate_transportMethod_true_noText() {
    var form = new LocationDetailsForm();
    form.setTransportsMaterialsToShore(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(Set.of(LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE)));
    assertThat(result).containsKeys("transportationMethod");
    assertThat(result).doesNotContainKeys("transportsMaterialsToShore");
  }

  @Test
  public void validate_transportMethod_false() {
    var form = new LocationDetailsForm();
    form.setTransportsMaterialsToShore(false);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(Set.of(LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE)));
    assertThat(result).doesNotContainKeys("transportsMaterialsToShore");
  }

  @Test
  public void validate_transportMethod_true_withText() {
    var form = new LocationDetailsForm();
    form.setTransportsMaterialsToShore(true);
    form.setTransportationMethod("Test");
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form,
        getValidationHints(Set.of(LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE)));
    assertThat(result).doesNotContainKeys("transportationMethod", "transportsMaterialsToShore");
  }

}