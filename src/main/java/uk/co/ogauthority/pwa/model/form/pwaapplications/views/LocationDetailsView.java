package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.locationdetails.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.enums.locationdetails.PsrNotification;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;

public class LocationDetailsView {

  private final String approximateProjectLocationFromShore;
  private final HseSafetyZone withinSafetyZone;
  private final PsrNotification psrNotificationSubmittedOption;
  private final String psrNotificationSubmissionDate;
  private final String psrNotificationNotRequiredReason;
  private final Boolean diversUsed;
  private final List<String> facilitiesIfYes;
  private final List<String> facilitiesIfPartially;
  private final Boolean facilitiesOffshore;
  private final Boolean transportsMaterialsToShore;
  
  private final String transportationMethod;

  private final String pipelineRouteDetails;
  private final Boolean routeSurveyUndertaken;
  private final String routeSurveyNotUndertakenReason;
  private final Boolean withinLimitsOfDeviation;

  private final String surveyConcludedDate;
  
  private final String pipelineAshoreLocation;

  private final List<UploadedFileView> uploadedLetterFileViews;


  public LocationDetailsView(String approximateProjectLocationFromShore,
                             HseSafetyZone withinSafetyZone,
                             PsrNotification psrNotificationSubmittedOption,
                             String psrNotificationSubmissionDate,
                             String psrNotificationNotRequiredReason,
                             Boolean diversUsed,
                             List<String> facilitiesIfYes,
                             List<String> facilitiesIfPartially,
                             Boolean facilitiesOffshore,
                             Boolean transportsMaterialsToShore,
                             String transportationMethod,
                             String pipelineRouteDetails,
                             Boolean routeSurveyUndertaken,
                             String routeSurveyNotUndertakenReason,
                             Boolean withinLimitsOfDeviation,
                             String surveyConcludedDate,
                             String pipelineAshoreLocation,
                             List<UploadedFileView> uploadedLetterFileViews) {
    this.approximateProjectLocationFromShore = approximateProjectLocationFromShore;
    this.withinSafetyZone = withinSafetyZone;
    this.psrNotificationSubmittedOption = psrNotificationSubmittedOption;
    this.psrNotificationSubmissionDate = psrNotificationSubmissionDate;
    this.psrNotificationNotRequiredReason = psrNotificationNotRequiredReason;
    this.diversUsed = diversUsed;
    this.facilitiesIfYes = facilitiesIfYes;
    this.facilitiesIfPartially = facilitiesIfPartially;
    this.facilitiesOffshore = facilitiesOffshore;
    this.transportsMaterialsToShore = transportsMaterialsToShore;
    this.transportationMethod = transportationMethod;
    this.pipelineRouteDetails = pipelineRouteDetails;
    this.routeSurveyUndertaken = routeSurveyUndertaken;
    this.routeSurveyNotUndertakenReason = routeSurveyNotUndertakenReason;
    this.withinLimitsOfDeviation = withinLimitsOfDeviation;
    this.surveyConcludedDate = surveyConcludedDate;
    this.pipelineAshoreLocation = pipelineAshoreLocation;
    this.uploadedLetterFileViews = uploadedLetterFileViews;
  }





  public String getApproximateProjectLocationFromShore() {
    return approximateProjectLocationFromShore;
  }

  public HseSafetyZone getWithinSafetyZone() {
    return withinSafetyZone;
  }

  public PsrNotification getPsrNotificationSubmittedOption() {
    return psrNotificationSubmittedOption;
  }

  public String getPsrNotificationSubmissionDate() {
    return psrNotificationSubmissionDate;
  }

  public String getPsrNotificationNotRequiredReason() {
    return psrNotificationNotRequiredReason;
  }

  public Boolean getDiversUsed() {
    return diversUsed;
  }

  public List<String> getFacilitiesIfYes() {
    return facilitiesIfYes;
  }

  public List<String> getFacilitiesIfPartially() {
    return facilitiesIfPartially;
  }

  public Boolean getFacilitiesOffshore() {
    return facilitiesOffshore;
  }

  public Boolean getTransportsMaterialsToShore() {
    return transportsMaterialsToShore;
  }

  public String getTransportationMethod() {
    return transportationMethod;
  }

  public String getPipelineRouteDetails() {
    return pipelineRouteDetails;
  }

  public Boolean getRouteSurveyUndertaken() {
    return routeSurveyUndertaken;
  }

  public String getRouteSurveyNotUndertakenReason() {
    return routeSurveyNotUndertakenReason;
  }

  public Boolean getWithinLimitsOfDeviation() {
    return withinLimitsOfDeviation;
  }

  public String getSurveyConcludedDate() {
    return surveyConcludedDate;
  }

  public String getPipelineAshoreLocation() {
    return pipelineAshoreLocation;
  }

  public List<UploadedFileView> getUploadedLetterFileViews() {
    return uploadedLetterFileViews;
  }
}
