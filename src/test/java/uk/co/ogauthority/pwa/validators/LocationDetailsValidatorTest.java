package uk.co.ogauthority.pwa.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

public class LocationDetailsValidatorTest {

  private LocationDetailsValidator locationDetailsValidator;

  @Before
  public void setUp() {
    locationDetailsValidator = new LocationDetailsValidator();
  }

  @Test
  public void validate_Nulls() {
    var form = new LocationDetailsForm();
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("withinSafetyZone", Set.of("withinSafetyZone" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("transportsMaterialsToShore",
            Set.of("transportsMaterialsToShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_EmptyText() {
    var form = new LocationDetailsForm();
    form.setApproximateProjectLocationFromShore("");
    form.setTransportationMethod("");
    form.setPipelineRouteDetails("");
    form.setTransportsMaterialsToShore(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("withinSafetyZone", Set.of("withinSafetyZone" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("transportationMethod", Set.of("transportationMethod" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_WithinSafetyZone_No() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("transportsMaterialsToShore",
            Set.of("transportsMaterialsToShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_WithinSafetyZone_Partially_Null_Facilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("transportsMaterialsToShore",
            Set.of("transportsMaterialsToShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesIfPartially", Set.of("facilitiesIfPartially" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_WithinSafetyZone_Partially_With_Facilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    form.setFacilitiesIfPartially(List.of("1"));
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("transportsMaterialsToShore",
            Set.of("transportsMaterialsToShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_WithinSafetyZone_Yes_Null_Facilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("transportsMaterialsToShore",
            Set.of("transportsMaterialsToShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesIfYes", Set.of("facilitiesIfYes" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_WithinSafetyZone_Yes_With_Facilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    form.setFacilitiesIfYes(List.of("1"));
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("transportsMaterialsToShore",
            Set.of("transportsMaterialsToShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_SurveyConcludedDate_Nulls() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).contains(
        entry("surveyConcludedDay", Set.of("surveyConcludedDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("surveyConcludedMonth", Set.of("surveyConcludedMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("surveyConcludedYear", Set.of("surveyConcludedYear" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  public void validate_SurveyConcludedDate_Valid() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    form.setSurveyConcludedDay(1);
    form.setSurveyConcludedMonth(1);
    form.setSurveyConcludedYear(2020);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).doesNotContainKeys("surveyConcludedDay", "surveyConcludedMonth", "surveyConcludedYear");
  }

  @Test
  public void validate_Valid() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    form.setTransportsMaterialsToShore(true);
    form.setApproximateProjectLocationFromShore("Approx");
    form.setTransportationMethod("Method");
    form.setFacilitiesOffshore(true);
    form.setRouteSurveyUndertaken(false);
    form.setPipelineRouteDetails("Detail text");
    form.setWithinLimitsOfDeviation(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).isEmpty();
  }

  @Test
  public void validatePartial_Nulls() {
    var form = new LocationDetailsForm();
    var errors = new BeanPropertyBindingResult(form, "form");
    locationDetailsValidator.validatePartial(form, errors);
    var result = errors.getFieldErrors().stream()
        .collect(
            Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
    assertThat(result).isEmpty();
  }

  @Test
  public void validatePartial_InvalidDate() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    form.setSurveyConcludedDay(1);

    var errors = new BeanPropertyBindingResult(form, "form");
    locationDetailsValidator.validatePartial(form, errors);
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
  public void validatePartial_Valid() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    form.setSurveyConcludedDay(1);
    form.setSurveyConcludedMonth(1);
    form.setSurveyConcludedYear(2020);

    var errors = new BeanPropertyBindingResult(form, "form");
    locationDetailsValidator.validatePartial(form, errors);
    var result = errors.getFieldErrors().stream()
        .collect(
            Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
    assertThat(result).isEmpty();
  }

  @Test
  public void validate_TransportMethod_True_NoText() {
    var form = new LocationDetailsForm();
    form.setTransportsMaterialsToShore(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("withinSafetyZone", Set.of("withinSafetyZone" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("transportationMethod", Set.of("transportationMethod" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_TransportMethod_True_WithText() {
    var form = new LocationDetailsForm();
    form.setTransportsMaterialsToShore(true);
    form.setTransportationMethod("Test");
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("withinSafetyZone", Set.of("withinSafetyZone" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_TransportMethod_False_NoText() {
    var form = new LocationDetailsForm();
    form.setTransportsMaterialsToShore(false);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("withinSafetyZone", Set.of("withinSafetyZone" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelineAshoreLocation", Set.of("pipelineAshoreLocation" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_TransportMethod_False_WithText() {
    var form = new LocationDetailsForm();
    form.setTransportsMaterialsToShore(false);
    form.setPipelineAshoreLocation("Test");
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("withinSafetyZone", Set.of("withinSafetyZone" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("approximateProjectLocationFromShore",
            Set.of("approximateProjectLocationFromShore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("withinLimitsOfDeviation",
            Set.of("withinLimitsOfDeviation" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

}