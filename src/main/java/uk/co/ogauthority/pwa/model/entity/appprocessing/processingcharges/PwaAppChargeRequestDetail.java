package uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges;

import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;

@Entity
@Table(name = "pwa_app_charge_request_details")
public class PwaAppChargeRequestDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", name = "pwa_app_charge_request_id")
  private PwaAppChargeRequest pwaAppChargeRequestId;

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

  private Integer autoCaseOfficerPersonId;
  private Integer totalPennies;
  private String chargeSummary;

  @Enumerated(EnumType.STRING)
  private PwaAppChargeRequestStatus status;

  private String chargeWaivedReason;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public PwaAppChargeRequest getPwaAppChargeRequestId() {
    return pwaAppChargeRequestId;
  }

  public void setPwaAppChargeRequestId(PwaAppChargeRequest pwaAppChargeRequestId) {
    this.pwaAppChargeRequestId = pwaAppChargeRequestId;
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


  public Integer getAutoCaseOfficerPersonId() {
    return autoCaseOfficerPersonId;
  }

  public void setAutoCaseOfficerPersonId(Integer autoCaseOfficerPersonId) {
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


  public PwaAppChargeRequestStatus getStatus() {
    return status;
  }

  public void setStatus(PwaAppChargeRequestStatus status) {
    this.status = status;
  }


  public String getChargeWaivedReason() {
    return chargeWaivedReason;
  }

  public void setChargeWaivedReason(String chargeWaivedReason) {
    this.chargeWaivedReason = chargeWaivedReason;
  }

}
