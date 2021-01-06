package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;

public class LocationDetailsForm extends UploadMultipleFilesWithDescriptionForm {

  private String approximateProjectLocationFromShore;
  private LocationDetailsSafetyZoneForm safetyZoneQuestionForm = new LocationDetailsSafetyZoneForm();
  private Boolean facilitiesOffshore;
  private Boolean transportsMaterialsToShore;

  private String transportationMethod;

  private String pipelineRouteDetails;
  private Boolean routeSurveyUndertaken;
  private Boolean withinLimitsOfDeviation;

  private Integer surveyConcludedDay;
  private Integer surveyConcludedMonth;
  private Integer surveyConcludedYear;

  private String pipelineAshoreLocation;


  public String getApproximateProjectLocationFromShore() {
    return approximateProjectLocationFromShore;
  }

  public void setApproximateProjectLocationFromShore(String approximateProjectLocationFromShore) {
    this.approximateProjectLocationFromShore = approximateProjectLocationFromShore;
  }

  public LocationDetailsSafetyZoneForm getSafetyZoneQuestionForm() {
    return safetyZoneQuestionForm;
  }

  public void setSafetyZoneQuestionForm(
      LocationDetailsSafetyZoneForm safetyZoneQuestionForm) {
    this.safetyZoneQuestionForm = safetyZoneQuestionForm;
  }

  public Boolean getFacilitiesOffshore() {
    return facilitiesOffshore;
  }

  public void setFacilitiesOffshore(Boolean facilitiesOffshore) {
    this.facilitiesOffshore = facilitiesOffshore;
  }

  public Boolean getTransportsMaterialsToShore() {
    return transportsMaterialsToShore;
  }

  public void setTransportsMaterialsToShore(Boolean transportsMaterialsToShore) {
    this.transportsMaterialsToShore = transportsMaterialsToShore;
  }

  public String getTransportationMethod() {
    return transportationMethod;
  }

  public void setTransportationMethod(String transportationMethod) {
    this.transportationMethod = transportationMethod;
  }

  public String getPipelineRouteDetails() {
    return pipelineRouteDetails;
  }

  public void setPipelineRouteDetails(String pipelineRouteDetails) {
    this.pipelineRouteDetails = pipelineRouteDetails;
  }

  public Boolean getRouteSurveyUndertaken() {
    return routeSurveyUndertaken;
  }

  public void setRouteSurveyUndertaken(Boolean routeSurveyUndertaken) {
    this.routeSurveyUndertaken = routeSurveyUndertaken;
  }

  public Boolean getWithinLimitsOfDeviation() {
    return withinLimitsOfDeviation;
  }

  public void setWithinLimitsOfDeviation(Boolean withinLimitsOfDeviation) {
    this.withinLimitsOfDeviation = withinLimitsOfDeviation;
  }

  public Integer getSurveyConcludedDay() {
    return surveyConcludedDay;
  }

  public void setSurveyConcludedDay(Integer surveyConcludedDay) {
    this.surveyConcludedDay = surveyConcludedDay;
  }

  public Integer getSurveyConcludedMonth() {
    return surveyConcludedMonth;
  }

  public void setSurveyConcludedMonth(Integer surveyConcludedMonth) {
    this.surveyConcludedMonth = surveyConcludedMonth;
  }

  public Integer getSurveyConcludedYear() {
    return surveyConcludedYear;
  }

  public void setSurveyConcludedYear(Integer surveyConcludedYear) {
    this.surveyConcludedYear = surveyConcludedYear;
  }

  public String getPipelineAshoreLocation() {
    return pipelineAshoreLocation;
  }

  public void setPipelineAshoreLocation(String pipelineAshoreLocation) {
    this.pipelineAshoreLocation = pipelineAshoreLocation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocationDetailsForm that = (LocationDetailsForm) o;
    return Objects.equals(approximateProjectLocationFromShore, that.approximateProjectLocationFromShore)
        && Objects.equals(safetyZoneQuestionForm, that.safetyZoneQuestionForm)
        && Objects.equals(facilitiesOffshore, that.facilitiesOffshore)
        && Objects.equals(transportsMaterialsToShore, that.transportsMaterialsToShore)
        && Objects.equals(transportationMethod, that.transportationMethod)
        && Objects.equals(pipelineRouteDetails, that.pipelineRouteDetails)
        && Objects.equals(routeSurveyUndertaken, that.routeSurveyUndertaken)
        && Objects.equals(withinLimitsOfDeviation, that.withinLimitsOfDeviation)
        && Objects.equals(surveyConcludedDay, that.surveyConcludedDay)
        && Objects.equals(surveyConcludedMonth, that.surveyConcludedMonth)
        && Objects.equals(surveyConcludedYear, that.surveyConcludedYear)
        && Objects.equals(pipelineAshoreLocation, that.pipelineAshoreLocation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(approximateProjectLocationFromShore, safetyZoneQuestionForm,
        facilitiesOffshore, transportsMaterialsToShore, transportationMethod, pipelineRouteDetails,
        routeSurveyUndertaken,
        withinLimitsOfDeviation, surveyConcludedDay, surveyConcludedMonth, surveyConcludedYear, pipelineAshoreLocation);
  }
}
