package uk.co.ogauthority.pwa.service.pwaapplications.events;

import jakarta.persistence.Column;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;

@Entity
@Table(name = "pwa_application_events")
public class PwaApplicationEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "pwa_application_id")
  @ManyToOne
  private PwaApplication pwaApplication;

  @Enumerated(EnumType.STRING)
  private PwaApplicationEventType eventType;

  @Column(name = "event_timestamp")
  private Instant eventInstant;

  @Column(name = "event_cleared_timestamp")
  private Instant eventClearedInstant;

  private String message;

  private Integer eventWuaId;

  public PwaApplicationEvent(PwaApplication pwaApplication,
                             PwaApplicationEventType eventType,
                             Instant eventInstant,
                             WebUserAccount eventUser) {
    this.pwaApplication = pwaApplication;
    this.eventType = eventType;
    this.eventInstant = eventInstant;
    this.eventWuaId = eventUser.getWuaId();
  }

  public PwaApplicationEvent() {

  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }

  public void setPwaApplication(PwaApplication pwaApplication) {
    this.pwaApplication = pwaApplication;
  }

  public PwaApplicationEventType getEventType() {
    return eventType;
  }

  public void setEventType(PwaApplicationEventType eventType) {
    this.eventType = eventType;
  }

  public Instant getEventInstant() {
    return eventInstant;
  }

  public void setEventInstant(Instant eventInstant) {
    this.eventInstant = eventInstant;
  }

  public Instant getEventClearedInstant() {
    return eventClearedInstant;
  }

  public void setEventClearedInstant(Instant eventClearedInstant) {
    this.eventClearedInstant = eventClearedInstant;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Integer getEventWuaId() {
    return eventWuaId;
  }

  public void setEventWuaId(Integer eventWuaId) {
    this.eventWuaId = eventWuaId;
  }

}
