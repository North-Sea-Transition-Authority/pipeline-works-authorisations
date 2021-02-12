package uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges;

import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;

@Entity
@Table(name = "pwa_app_charge_payment_attempt")
public class PwaAppChargePaymentAttempt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", name = "pwa_app_charge_request_id")
  private PwaAppChargeRequest pwaAppChargeRequest;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "created_by_person_id")
  private PersonId createdByPersonId;
  private Instant createdTimestamp;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "ended_by_person_id")
  private PersonId endedByPersonId;
  private Instant endedTimestamp;

  private Integer activeFlag;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public PwaAppChargeRequest getPwaAppChargeRequest() {
    return pwaAppChargeRequest;
  }

  public void setPwaAppChargeRequest(PwaAppChargeRequest pwaAppChargeRequest) {
    this.pwaAppChargeRequest = pwaAppChargeRequest;
  }


  public PersonId getCreatedByPersonId() {
    return createdByPersonId;
  }

  public void setCreatedByPersonId(PersonId createdByPersonId) {
    this.createdByPersonId = createdByPersonId;
  }


  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }


  public PersonId getEndedByPersonId() {
    return endedByPersonId;
  }

  public void setEndedByPersonId(PersonId endedByPersonId) {
    this.endedByPersonId = endedByPersonId;
  }


  public Instant getEndedTimestamp() {
    return endedTimestamp;
  }

  public void setEndedTimestamp(Instant endedTimestamp) {
    this.endedTimestamp = endedTimestamp;
  }


  public Integer getActiveFlag() {
    return activeFlag;
  }

  public void setActiveFlag(Integer activeFlag) {
    this.activeFlag = activeFlag;
  }

}
