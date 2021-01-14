package uk.co.ogauthority.pwa.model.entity.pwaapplications.form;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity(name = "pad_location_details")
public class PadLocationDetails implements ChildEntity<Integer, PwaApplicationDetail> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @Column(name = "approximate_location")
  private String approximateProjectLocationFromShore;

  @Enumerated(EnumType.STRING)
  private HseSafetyZone withinSafetyZone;

  private Boolean psrNotificationSubmitted;

  @Column(name = "psr_submitted_month")
  private Integer psrNotificationSubmittedMonth;
  @Column(name = "psr_submitted_year")
  private Integer psrNotificationSubmittedYear;

  @Column(name = "psr_expected_submission_month")
  private Integer psrNotificationExpectedSubmissionMonth;
  @Column(name = "psr_expected_submission_year")
  private Integer psrNotificationExpectedSubmissionYear;

  private Boolean facilitiesOffshore;
  private Boolean transportsMaterialsToShore;
  private String transportationMethod;
  private String pipelineRouteDetails;
  private Instant surveyConcludedTimestamp;
  private Boolean routeSurveyUndertaken;
  @Column(name = "survey_not_undertaken_reason")
  private String routeSurveyNotUndertakenReason;
  private Boolean withinLimitsOfDeviation;
  private String pipelineAshoreLocation;

  // ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PwaApplicationDetail parentEntity) {
    this.pwaApplicationDetail = parentEntity;
  }

  @Override
  public PwaApplicationDetail getParent() {
    return this.pwaApplicationDetail;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public String getApproximateProjectLocationFromShore() {
    return approximateProjectLocationFromShore;
  }

  public void setApproximateProjectLocationFromShore(String approximateLocationFromShore) {
    this.approximateProjectLocationFromShore = approximateLocationFromShore;
  }

  public HseSafetyZone getWithinSafetyZone() {
    return withinSafetyZone;
  }

  public void setWithinSafetyZone(HseSafetyZone withinSafetyZone) {
    this.withinSafetyZone = withinSafetyZone;
  }

  public Boolean getPsrNotificationSubmitted() {
    return psrNotificationSubmitted;
  }

  public void setPsrNotificationSubmitted(Boolean psrNotificationSubmitted) {
    this.psrNotificationSubmitted = psrNotificationSubmitted;
  }

  public Integer getPsrNotificationSubmittedMonth() {
    return psrNotificationSubmittedMonth;
  }

  public void setPsrNotificationSubmittedMonth(Integer psrNotificationSubmittedMonth) {
    this.psrNotificationSubmittedMonth = psrNotificationSubmittedMonth;
  }

  public Integer getPsrNotificationSubmittedYear() {
    return psrNotificationSubmittedYear;
  }

  public void setPsrNotificationSubmittedYear(Integer psrNotificationSubmittedYear) {
    this.psrNotificationSubmittedYear = psrNotificationSubmittedYear;
  }

  public Integer getPsrNotificationExpectedSubmissionMonth() {
    return psrNotificationExpectedSubmissionMonth;
  }

  public void setPsrNotificationExpectedSubmissionMonth(Integer psrNotificationExpectedSubmissionMonth) {
    this.psrNotificationExpectedSubmissionMonth = psrNotificationExpectedSubmissionMonth;
  }

  public Integer getPsrNotificationExpectedSubmissionYear() {
    return psrNotificationExpectedSubmissionYear;
  }

  public void setPsrNotificationExpectedSubmissionYear(Integer psrNotificationExpectedSubmissionYear) {
    this.psrNotificationExpectedSubmissionYear = psrNotificationExpectedSubmissionYear;
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

  public Instant getSurveyConcludedTimestamp() {
    return surveyConcludedTimestamp;
  }

  public void setSurveyConcludedTimestamp(Instant surveyConcludedTimestamp) {
    this.surveyConcludedTimestamp = surveyConcludedTimestamp;
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

  public String getPipelineAshoreLocation() {
    return pipelineAshoreLocation;
  }

  public void setPipelineAshoreLocation(String pipelineAshoreLocation) {
    this.pipelineAshoreLocation = pipelineAshoreLocation;
  }
}
