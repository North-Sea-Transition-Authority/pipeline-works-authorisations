package uk.co.ogauthority.pwa.model.view.publicnotice;

import java.time.Instant;
import uk.co.ogauthority.pwa.util.DateUtils;

public class PublicNoticeEvent {

  private PublicNoticeEventType eventType;

  private String comment;

  private Instant eventTimestamp;

  private String eventTimestampString;

  private String personId;

  private String personName;

  public PublicNoticeEventType getEventType() {
    return eventType;
  }

  public PublicNoticeEvent setEventType(
      PublicNoticeEventType eventType) {
    this.eventType = eventType;
    return this;
  }

  public String getComment() {
    return comment;
  }

  public PublicNoticeEvent setComment(String comment) {
    this.comment = comment;
    return this;
  }

  public Instant getEventTimestamp() {
    return eventTimestamp;
  }

  public PublicNoticeEvent setEventTimestamp(Instant eventTimestamp) {
    this.eventTimestamp = eventTimestamp;
    this.eventTimestampString = DateUtils.formatDateTime(eventTimestamp);
    return this;
  }

  public String getEventTimestampString() {
    return eventTimestampString;
  }

  public String getPersonId() {
    return personId;
  }

  public String getPersonName() {
    return personName;
  }

  public PublicNoticeEvent setPersonId(String personId) {
    this.personId = personId;
    return this;
  }

  public PublicNoticeEvent setPersonName(String personName) {
    this.personName = personName;
    return this;
  }
}
