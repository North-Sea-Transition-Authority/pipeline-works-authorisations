package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;

public class LocationDetailsView {

  private final String approximateProjectLocationFromShore;
  private final HseSafetyZone withinSafetyZone;
  private final List<String> facilitiesIfYes;
  private final List<String> facilitiesIfPartially;
  private final Boolean facilitiesOffshore;
  private final Boolean transportsMaterialsToShore;
  
  private final String transportationMethod;

  private final String pipelineRouteDetails;
  private final Boolean routeSurveyUndertaken;
  private final Boolean withinLimitsOfDeviation;

  private final String surveyConcludedDate;
  
  private final String pipelineAshoreLocation;


  public LocationDetailsView(String approximateProjectLocationFromShore,
                             HseSafetyZone withinSafetyZone,
                             List<String> facilitiesIfYes, List<String> facilitiesIfPartially,
                             Boolean facilitiesOffshore,
                             Boolean transportsMaterialsToShore, String transportationMethod,
                             String pipelineRouteDetails, Boolean routeSurveyUndertaken,
                             Boolean withinLimitsOfDeviation, String surveyConcludedDate,
                             String pipelineAshoreLocation) {
    this.approximateProjectLocationFromShore = approximateProjectLocationFromShore;
    this.withinSafetyZone = withinSafetyZone;
    this.facilitiesIfYes = facilitiesIfYes;
    this.facilitiesIfPartially = facilitiesIfPartially;
    this.facilitiesOffshore = facilitiesOffshore;
    this.transportsMaterialsToShore = transportsMaterialsToShore;
    this.transportationMethod = transportationMethod;
    this.pipelineRouteDetails = pipelineRouteDetails;
    this.routeSurveyUndertaken = routeSurveyUndertaken;
    this.withinLimitsOfDeviation = withinLimitsOfDeviation;
    this.surveyConcludedDate = surveyConcludedDate;
    this.pipelineAshoreLocation = pipelineAshoreLocation;
  }


  public String getApproximateProjectLocationFromShore() {
    return approximateProjectLocationFromShore;
  }

  public HseSafetyZone getWithinSafetyZone() {
    return withinSafetyZone;
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

  public Boolean getWithinLimitsOfDeviation() {
    return withinLimitsOfDeviation;
  }

  public String getSurveyConcludedDate() {
    return surveyConcludedDate;
  }

  public String getPipelineAshoreLocation() {
    return pipelineAshoreLocation;
  }
}
