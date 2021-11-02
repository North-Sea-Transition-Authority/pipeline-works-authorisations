package uk.co.ogauthority.pwa.integrations.govuknotify;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotifyCallback {

  public enum NotifyCallbackStatus {

    @JsonProperty("permanent-failure")
    PERMANENT_FAILURE,

    @JsonProperty("temporary-failure")
    TEMPORARY_FAILURE,

    @JsonProperty("technical-failure")
    TECHNICAL_FAILURE,

    @JsonProperty("delivered")
    DELIVERED

  }

  public enum NotifyNotificationType {

    @JsonProperty("email")
    EMAIL,

    @JsonProperty("sms")
    SMS

  }

  private String id;

  private int reference;

  private NotifyCallbackStatus status;

  private String to;

  private NotifyNotificationType notificationType;

  private Instant createdAt;

  private Instant completedAt;

  private Instant sentAt;

  @JsonCreator
  public NotifyCallback(
      @JsonProperty(value = "id") String id,
      @JsonProperty(value = "reference") int reference,
      @JsonProperty(value = "status") NotifyCallbackStatus status,
      @JsonProperty(value = "to") String to,
      @JsonProperty(value = "notification_type") NotifyNotificationType notificationType,
      @JsonProperty(value = "created_at") Instant createdAt,
      @JsonProperty(value = "completed_at") Instant completedAt,
      @JsonProperty(value = "sent_at") Instant sentAt
  ) {
    this.id = id;
    this.reference = reference;
    this.status = status;
    this.to = to;
    this.notificationType = notificationType;
    this.createdAt = createdAt;
    this.completedAt = completedAt;
    this.sentAt = sentAt;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getReference() {
    return reference;
  }

  public void setReference(int reference) {
    this.reference = reference;
  }

  public NotifyCallbackStatus getStatus() {
    return status;
  }

  public void setStatus(NotifyCallbackStatus status) {
    this.status = status;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public NotifyNotificationType getNotificationType() {
    return notificationType;
  }

  public void setNotificationType(NotifyNotificationType notificationType) {
    this.notificationType = notificationType;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Instant completedAt) {
    this.completedAt = completedAt;
  }

  public Instant getSentAt() {
    return sentAt;
  }

  public void setSentAt(Instant sentAt) {
    this.sentAt = sentAt;
  }

  @Override
  public String toString() {
    return "NotifyCallback{" +
        "id='" + id + '\'' +
        ", reference=" + reference +
        ", status=" + status +
        ", to='" + to + '\'' +
        ", notificationType='" + notificationType + '\'' +
        ", createdAt=" + createdAt + '\'' +
        ", completedAt=" + completedAt + '\'' +
        ", sentAt=" + sentAt + '\'' +
        '}';
  }
}