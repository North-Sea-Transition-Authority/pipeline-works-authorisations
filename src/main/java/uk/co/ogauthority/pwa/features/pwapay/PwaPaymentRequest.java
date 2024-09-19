package uk.co.ogauthority.pwa.features.pwapay;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;
import uk.co.ogauthority.pwa.integrations.govukpay.GovUkPaymentStatus;

@Entity
@Audited
@Table(name = "pwa_payment_requests")
public class PwaPaymentRequest {

  @Id
  @GeneratedValue(generator = "uuid2")
  @JdbcTypeCode(SqlTypes.CHAR)
  private UUID uuid;

  @Enumerated(EnumType.STRING)
  private PaymentRequestType requestedService;

  private Integer amountPennies;

  private Instant createdTimestamp;
  private String reference;
  private String description;

  private String returnUrl;

  @Enumerated(EnumType.STRING)
  private PaymentRequestStatus requestStatus;
  private Instant requestStatusTimestamp;

  private String requestStatusMessage;

  private String govUkPaymentId;

  @Enumerated(EnumType.STRING)
  private GovUkPaymentStatus govUkPaymentStatus;

  @Column(name = "gov_uk_payment_status_ts")
  private Instant govUkPaymentStatusTimestamp;

  private String govUkPaymentStatusMessage;

  // object helper methods
  boolean hasGovUkPaymentId() {
    return StringUtils.isNotBlank(this.govUkPaymentId);
  }

  boolean isInJourneyState(PaymentRequestStatus.JourneyState journeyState) {
    return this.requestStatus.getJourneyState().equals(journeyState);
  }

  // GETTERS & SETTERS

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public PaymentRequestType getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(PaymentRequestType requestedService) {
    this.requestedService = requestedService;
  }

  public Integer getAmountPennies() {
    return amountPennies;
  }

  public void setAmountPennies(Integer amountPennies) {
    this.amountPennies = amountPennies;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getReturnUrl() {
    return returnUrl;
  }

  public void setReturnUrl(String returnUrl) {
    this.returnUrl = returnUrl;
  }

  public PaymentRequestStatus getRequestStatus() {
    return requestStatus;
  }

  public void setRequestStatus(PaymentRequestStatus requestStatus) {
    this.requestStatus = requestStatus;
  }

  public Instant getRequestStatusTimestamp() {
    return requestStatusTimestamp;
  }

  public void setRequestStatusTimestamp(Instant requestStatusTimestamp) {
    this.requestStatusTimestamp = requestStatusTimestamp;
  }

  public String getRequestStatusMessage() {
    return requestStatusMessage;
  }

  public void setRequestStatusMessage(String requestStatusMessage) {
    this.requestStatusMessage = requestStatusMessage;
  }

  public String getGovUkPaymentId() {
    return govUkPaymentId;
  }

  public void setGovUkPaymentId(String govUkPaymentId) {
    this.govUkPaymentId = govUkPaymentId;
  }

  public GovUkPaymentStatus getGovUkPaymentStatus() {
    return govUkPaymentStatus;
  }

  public void setGovUkPaymentStatus(GovUkPaymentStatus govUkPaymentStatus) {
    this.govUkPaymentStatus = govUkPaymentStatus;
  }

  public Instant getGovUkPaymentStatusTimestamp() {
    return govUkPaymentStatusTimestamp;
  }

  public void setGovUkPaymentStatusTimestamp(Instant govUkPaymentStatusTimestamp) {
    this.govUkPaymentStatusTimestamp = govUkPaymentStatusTimestamp;
  }

  public String getGovUkPaymentStatusMessage() {
    return govUkPaymentStatusMessage;
  }

  public void setGovUkPaymentStatusMessage(String govUkPaymentStatusMessage) {
    this.govUkPaymentStatusMessage = govUkPaymentStatusMessage;
  }
}
