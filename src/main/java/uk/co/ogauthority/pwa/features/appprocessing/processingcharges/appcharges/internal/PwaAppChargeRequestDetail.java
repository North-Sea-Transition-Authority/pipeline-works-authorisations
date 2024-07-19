package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;

@Entity
@Table(name = "pwa_app_charge_request_details")
public class PwaAppChargeRequestDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", name = "pwa_app_charge_request_id")
  private PwaAppChargeRequest pwaAppChargeRequest;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "started_by_person_id")
  private PersonId startedByPersonId;
  private Instant startedTimestamp;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "ended_by_person_id")
  private PersonId endedByPersonId;
  private Instant endedTimestamp;

  private Boolean tipFlag;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "auto_case_officer_person_id")
  private PersonId autoCaseOfficerPersonId;

  private Integer totalPennies;
  private String chargeSummary;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private PwaAppChargeRequestStatus pwaAppChargeRequestStatus;

  private String chargeWaivedReason;

  private String chargeCancelledReason;

  public PwaAppChargeRequestDetail() {
    // default
  }

  public PwaAppChargeRequestDetail(PwaAppChargeRequest pwaAppChargeRequest) {
    this.pwaAppChargeRequest = pwaAppChargeRequest;
    this.startedByPersonId = pwaAppChargeRequest.getRequestedByPersonId();
    this.startedTimestamp = pwaAppChargeRequest.getRequestedByTimestamp();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public PwaAppChargeRequest getPwaAppChargeRequest() {
    return pwaAppChargeRequest;
  }

  public void setPwaAppChargeRequest(PwaAppChargeRequest pwaAppChargeRequestId) {
    this.pwaAppChargeRequest = pwaAppChargeRequestId;
  }


  public Instant getStartedTimestamp() {
    return startedTimestamp;
  }

  public void setStartedTimestamp(Instant startedTimestamp) {
    this.startedTimestamp = startedTimestamp;
  }


  public PersonId getStartedByPersonId() {
    return startedByPersonId;
  }

  public void setStartedByPersonId(PersonId startedByPersonId) {
    this.startedByPersonId = startedByPersonId;
  }


  public Instant getEndedTimestamp() {
    return endedTimestamp;
  }

  public void setEndedTimestamp(Instant endedTimestamp) {
    this.endedTimestamp = endedTimestamp;
  }


  public PersonId getEndedByPersonId() {
    return endedByPersonId;
  }

  public void setEndedByPersonId(PersonId endedByPersonId) {
    this.endedByPersonId = endedByPersonId;
  }


  public Boolean getTipFlag() {
    return tipFlag;
  }

  public void setTipFlag(Boolean tipFlag) {
    this.tipFlag = tipFlag;
  }


  public PersonId getAutoCaseOfficerPersonId() {
    return autoCaseOfficerPersonId;
  }

  public void setAutoCaseOfficerPersonId(PersonId autoCaseOfficerPersonId) {
    this.autoCaseOfficerPersonId = autoCaseOfficerPersonId;
  }


  public Integer getTotalPennies() {
    return totalPennies;
  }

  public void setTotalPennies(Integer totalPennies) {
    this.totalPennies = totalPennies;
  }


  public String getChargeSummary() {
    return chargeSummary;
  }

  public void setChargeSummary(String chargeSummary) {
    this.chargeSummary = chargeSummary;
  }


  public PwaAppChargeRequestStatus getPwaAppChargeRequestStatus() {
    return pwaAppChargeRequestStatus;
  }

  public void setPwaAppChargeRequestStatus(PwaAppChargeRequestStatus status) {
    this.pwaAppChargeRequestStatus = status;
  }


  public String getChargeWaivedReason() {
    return chargeWaivedReason;
  }

  public void setChargeWaivedReason(String chargeWaivedReason) {
    this.chargeWaivedReason = chargeWaivedReason;
  }

  public String getChargeCancelledReason() {
    return chargeCancelledReason;
  }

  public void setChargeCancelledReason(String chargeCancelledReason) {
    this.chargeCancelledReason = chargeCancelledReason;
  }
}
