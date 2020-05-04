package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.validator.constraints.Length;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;

public class LocationDetailsForm extends UploadMultipleFilesWithDescriptionForm {

  @Length(max = 4000, message = "Approximate project location from shore must be 4000 characters or fewer")
  private String approximateProjectLocationFromShore;
  private HseSafetyZone withinSafetyZone;
  private List<String> facilitiesIfYes;
  private List<String> facilitiesIfPartially;
  private Boolean facilitiesOffshore;
  private Boolean transportsMaterialsToShore;

  @Length(max = 4000, message = "Transportation method must be 4000 characters or fewer")
  private String transportationMethod;

  private String pipelineRouteDetails;
  private Boolean routeSurveyUndertaken;
  private Boolean withinLimitsOfDeviation;

  private Integer surveyConcludedDay;
  private Integer surveyConcludedMonth;
  private Integer surveyConcludedYear;

  public LocationDetailsForm() {
    facilitiesIfPartially = new ArrayList<>();
    facilitiesIfYes = new ArrayList<>();
  }

  public String getApproximateProjectLocationFromShore() {
    return approximateProjectLocationFromShore;
  }

  public void setApproximateProjectLocationFromShore(String approximateProjectLocationFromShore) {
    this.approximateProjectLocationFromShore = approximateProjectLocationFromShore;
  }

  public HseSafetyZone getWithinSafetyZone() {
    return withinSafetyZone;
  }

  public void setWithinSafetyZone(HseSafetyZone withinSafetyZone) {
    this.withinSafetyZone = withinSafetyZone;
  }

  public List<String> getFacilitiesIfYes() {
    return facilitiesIfYes;
  }

  public void setFacilitiesIfYes(List<String> facilitiesIfYes) {
    this.facilitiesIfYes = facilitiesIfYes;
  }

  public List<String> getFacilitiesIfPartially() {
    return facilitiesIfPartially;
  }

  public void setFacilitiesIfPartially(List<String> facilitiesIfPartially) {
    this.facilitiesIfPartially = facilitiesIfPartially;
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
}
