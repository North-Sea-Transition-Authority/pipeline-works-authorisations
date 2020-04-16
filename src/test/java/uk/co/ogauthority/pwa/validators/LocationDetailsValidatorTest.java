package uk.co.ogauthority.pwa.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.LocationDetailsForm;
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
        entry("withinSafetyZone", Set.of("withinSafetyZone.required")),
        entry("transportsMaterialsToShore", Set.of("transportsMaterialsToShore.required")),
        entry("approximateProjectLocationFromShore", Set.of("approximateProjectLocationFromShore.required")),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore.required")),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails.required")),
        entry("withinLimitsOfDeviation", Set.of("withinLimitsOfDeviation.required")),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken.required"))
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
        entry("withinSafetyZone", Set.of("withinSafetyZone.required")),
        entry("transportationMethod", Set.of("transportationMethod.required")),
        entry("approximateProjectLocationFromShore", Set.of("approximateProjectLocationFromShore.required")),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore.required")),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails.required")),
        entry("withinLimitsOfDeviation", Set.of("withinLimitsOfDeviation.required")),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken.required"))
    );
  }

  @Test
  public void validate_WithinSafetyZone_No() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("transportsMaterialsToShore", Set.of("transportsMaterialsToShore.required")),
        entry("approximateProjectLocationFromShore", Set.of("approximateProjectLocationFromShore.required")),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore.required")),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails.required")),
        entry("withinLimitsOfDeviation", Set.of("withinLimitsOfDeviation.required")),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken.required"))
    );
  }

  @Test
  public void validate_WithinSafetyZone_Partially_Null_Facilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("transportsMaterialsToShore", Set.of("transportsMaterialsToShore.required")),
        entry("facilitiesIfPartially", Set.of("facilitiesIfPartially.required")),
        entry("approximateProjectLocationFromShore", Set.of("approximateProjectLocationFromShore.required")),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore.required")),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails.required")),
        entry("withinLimitsOfDeviation", Set.of("withinLimitsOfDeviation.required")),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken.required"))
    );
  }

  @Test
  public void validate_WithinSafetyZone_Partially_With_Facilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    form.setFacilitiesIfPartially(List.of(new DevukFacility()));
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("transportsMaterialsToShore", Set.of("transportsMaterialsToShore.required")),
        entry("approximateProjectLocationFromShore", Set.of("approximateProjectLocationFromShore.required")),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore.required")),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails.required")),
        entry("withinLimitsOfDeviation", Set.of("withinLimitsOfDeviation.required")),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken.required"))
    );
  }

  @Test
  public void validate_WithinSafetyZone_Yes_Null_Facilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("transportsMaterialsToShore", Set.of("transportsMaterialsToShore.required")),
        entry("facilitiesIfYes", Set.of("facilitiesIfYes.required")),
        entry("approximateProjectLocationFromShore", Set.of("approximateProjectLocationFromShore.required")),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore.required")),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails.required")),
        entry("withinLimitsOfDeviation", Set.of("withinLimitsOfDeviation.required")),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken.required"))
    );
  }

  @Test
  public void validate_WithinSafetyZone_Yes_With_Facilities() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    form.setFacilitiesIfYes(List.of(new DevukFacility()));
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).containsOnly(
        entry("transportsMaterialsToShore", Set.of("transportsMaterialsToShore.required")),
        entry("approximateProjectLocationFromShore", Set.of("approximateProjectLocationFromShore.required")),
        entry("facilitiesOffshore", Set.of("facilitiesOffshore.required")),
        entry("pipelineRouteDetails", Set.of("pipelineRouteDetails.required")),
        entry("withinLimitsOfDeviation", Set.of("withinLimitsOfDeviation.required")),
        entry("routeSurveyUndertaken", Set.of("routeSurveyUndertaken.required"))
    );
  }

  @Test
  public void validate_SurveyConcludedDate_Nulls() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(true);
    var result = ValidatorTestUtils.getFormValidationErrors(locationDetailsValidator, form);
    assertThat(result).contains(
        entry("surveyConcludedDay", Set.of("surveyConcludedDay.invalid")),
        entry("surveyConcludedMonth", Set.of("surveyConcludedMonth.invalid")),
        entry("surveyConcludedYear", Set.of("surveyConcludedYear.invalid"))
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

}