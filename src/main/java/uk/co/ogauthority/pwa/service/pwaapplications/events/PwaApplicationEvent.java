package uk.co.ogauthority.pwa.service.pwaapplications.events;

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
import javax.persistence.Table;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

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
