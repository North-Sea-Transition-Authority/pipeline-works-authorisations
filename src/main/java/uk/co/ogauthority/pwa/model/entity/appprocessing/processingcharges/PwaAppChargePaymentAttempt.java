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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.pwapay.PaymentRequestStatus;
import uk.co.ogauthority.pwa.pwapay.PwaPaymentRequest;

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

  private Boolean activeFlag;

  @OneToOne
  @JoinColumn(referencedColumnName = "uuid", name = "pwa_payment_request_uuid")
  private PwaPaymentRequest pwaPaymentRequest;


  public PwaAppChargePaymentAttempt() {
    //default
  }

  public PwaAppChargePaymentAttempt(PwaAppChargeRequest pwaAppChargeRequest,
                                    PersonId createdByPersonId,
                                    Instant createdTimestamp,
                                    Boolean activeFlag,
                                    PwaPaymentRequest pwaPaymentRequest) {
    this.pwaAppChargeRequest = pwaAppChargeRequest;
    this.createdByPersonId = createdByPersonId;
    this.createdTimestamp = createdTimestamp;
    this.activeFlag = activeFlag;
    this.pwaPaymentRequest = pwaPaymentRequest;
  }

  // helper methods
  public PaymentRequestStatus getAssociatedPaymentRequestStatus() {
    return this.pwaPaymentRequest.getRequestStatus();
  }

  // getters and setters

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


  public Boolean getActiveFlag() {
    return activeFlag;
  }

  public void setActiveFlag(Boolean activeFlag) {
    this.activeFlag = activeFlag;
  }

  public PwaPaymentRequest getPwaPaymentRequest() {
    return pwaPaymentRequest;
  }

  public void setPwaPaymentRequest(PwaPaymentRequest pwaPaymentRequest) {
    this.pwaPaymentRequest = pwaPaymentRequest;
  }
}
