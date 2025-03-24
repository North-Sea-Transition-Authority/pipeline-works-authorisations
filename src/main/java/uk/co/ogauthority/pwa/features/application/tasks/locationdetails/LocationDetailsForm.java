package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import java.util.Objects;
import uk.co.ogauthority.pwa.features.filemanagement.FileUploadForm;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

public class LocationDetailsForm extends FileUploadForm {

  private String approximateProjectLocationFromShore;

  private HseSafetyZone withinSafetyZone;
  private LocationDetailsSafetyZoneForm completelyWithinSafetyZoneForm = new LocationDetailsSafetyZoneForm();
  private LocationDetailsSafetyZoneForm partiallyWithinSafetyZoneForm = new LocationDetailsSafetyZoneForm();

  private PsrNotification psrNotificationSubmittedOption;
  private TwoFieldDateInput psrNotificationSubmittedDate;
  private TwoFieldDateInput psrNotificationExpectedSubmissionDate;
  private String psrNotificationNotRequiredReason;

  private Boolean diversUsed;

  private Boolean facilitiesOffshore;
  private Boolean transportsMaterialsToShore;

  private Boolean transportsMaterialsFromShore;

  private String transportationMethodToShore;

  private String transportationMethodFromShore;

  private String pipelineRouteDetails;
  private Boolean routeSurveyUndertaken;
  private String routeSurveyNotUndertakenReason;
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

  public HseSafetyZone getWithinSafetyZone() {
    return withinSafetyZone;
  }

  public void setWithinSafetyZone(HseSafetyZone withinSafetyZone) {
    this.withinSafetyZone = withinSafetyZone;
  }

  public LocationDetailsSafetyZoneForm getCompletelyWithinSafetyZoneForm() {
    return completelyWithinSafetyZoneForm;
  }

  public void setCompletelyWithinSafetyZoneForm(
      LocationDetailsSafetyZoneForm completelyWithinSafetyZoneForm) {
    this.completelyWithinSafetyZoneForm = completelyWithinSafetyZoneForm;
  }

  public LocationDetailsSafetyZoneForm getPartiallyWithinSafetyZoneForm() {
    return partiallyWithinSafetyZoneForm;
  }

  public void setPartiallyWithinSafetyZoneForm(
      LocationDetailsSafetyZoneForm partiallyWithinSafetyZoneForm) {
    this.partiallyWithinSafetyZoneForm = partiallyWithinSafetyZoneForm;
  }

  public PsrNotification getPsrNotificationSubmittedOption() {
    return psrNotificationSubmittedOption;
  }

  public void setPsrNotificationSubmittedOption(
      PsrNotification psrNotificationSubmittedOption) {
    this.psrNotificationSubmittedOption = psrNotificationSubmittedOption;
  }

  public TwoFieldDateInput getPsrNotificationSubmittedDate() {
    return psrNotificationSubmittedDate;
  }

  public void setPsrNotificationSubmittedDate(
      TwoFieldDateInput psrNotificationSubmittedDate) {
    this.psrNotificationSubmittedDate = psrNotificationSubmittedDate;
  }

  public TwoFieldDateInput getPsrNotificationExpectedSubmissionDate() {
    return psrNotificationExpectedSubmissionDate;
  }

  public void setPsrNotificationExpectedSubmissionDate(
      TwoFieldDateInput psrNotificationExpectedSubmissionDate) {
    this.psrNotificationExpectedSubmissionDate = psrNotificationExpectedSubmissionDate;
  }

  public String getPsrNotificationNotRequiredReason() {
    return psrNotificationNotRequiredReason;
  }

  public void setPsrNotificationNotRequiredReason(String psrNotificationNotRequiredReason) {
    this.psrNotificationNotRequiredReason = psrNotificationNotRequiredReason;
  }

  public Boolean getDiversUsed() {
    return diversUsed;
  }

  public void setDiversUsed(Boolean diversUsed) {
    this.diversUsed = diversUsed;
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

  public Boolean getTransportsMaterialsFromShore() {
    return transportsMaterialsFromShore;
  }

  public void setTransportsMaterialsFromShore(Boolean transportsMaterialsFromShore) {
    this.transportsMaterialsFromShore = transportsMaterialsFromShore;
  }

  public String getTransportationMethodToShore() {
    return transportationMethodToShore;
  }

  public void setTransportationMethodToShore(String transportationMethodToShore) {
    this.transportationMethodToShore = transportationMethodToShore;
  }

  public String getTransportationMethodFromShore() {
    return transportationMethodFromShore;
  }

  public void setTransportationMethodFromShore(String transportationMethodFromShore) {
    this.transportationMethodFromShore = transportationMethodFromShore;
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

  public String getRouteSurveyNotUndertakenReason() {
    return routeSurveyNotUndertakenReason;
  }

  public void setRouteSurveyNotUndertakenReason(String routeSurveyNotUndertakenReason) {
    this.routeSurveyNotUndertakenReason = routeSurveyNotUndertakenReason;
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
        && withinSafetyZone == that.withinSafetyZone
        && Objects.equals(completelyWithinSafetyZoneForm, that.completelyWithinSafetyZoneForm)
        && Objects.equals(partiallyWithinSafetyZoneForm, that.partiallyWithinSafetyZoneForm)
        && Objects.equals(psrNotificationSubmittedOption, that.psrNotificationSubmittedOption)
        && Objects.equals(psrNotificationSubmittedDate, that.psrNotificationSubmittedDate)
        && Objects.equals(psrNotificationExpectedSubmissionDate, that.psrNotificationExpectedSubmissionDate)
        && Objects.equals(psrNotificationNotRequiredReason, that.psrNotificationNotRequiredReason)
        && Objects.equals(diversUsed, that.diversUsed)
        && Objects.equals(facilitiesOffshore, that.facilitiesOffshore)
        && Objects.equals(transportsMaterialsToShore, that.transportsMaterialsToShore)
        && Objects.equals(transportsMaterialsFromShore, that.transportsMaterialsFromShore)
        && Objects.equals(transportationMethodFromShore, that.transportationMethodFromShore)
        && Objects.equals(transportationMethodToShore, that.transportationMethodToShore)
        && Objects.equals(pipelineRouteDetails, that.pipelineRouteDetails)
        && Objects.equals(routeSurveyUndertaken, that.routeSurveyUndertaken)
        && Objects.equals(routeSurveyNotUndertakenReason, that.routeSurveyNotUndertakenReason)
        && Objects.equals(withinLimitsOfDeviation, that.withinLimitsOfDeviation)
        && Objects.equals(surveyConcludedDay, that.surveyConcludedDay)
        && Objects.equals(surveyConcludedMonth, that.surveyConcludedMonth)
        && Objects.equals(surveyConcludedYear, that.surveyConcludedYear)
        && Objects.equals(pipelineAshoreLocation, that.pipelineAshoreLocation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(approximateProjectLocationFromShore, withinSafetyZone, completelyWithinSafetyZoneForm,
        partiallyWithinSafetyZoneForm, psrNotificationSubmittedOption, psrNotificationSubmittedDate,
        psrNotificationExpectedSubmissionDate, psrNotificationNotRequiredReason, diversUsed, facilitiesOffshore,
        transportsMaterialsToShore, transportsMaterialsFromShore, transportationMethodFromShore,
        transportationMethodToShore, pipelineRouteDetails, routeSurveyUndertaken, routeSurveyNotUndertakenReason,
        withinLimitsOfDeviation, surveyConcludedDay, surveyConcludedMonth, surveyConcludedYear, pipelineAshoreLocation);
  }
}
