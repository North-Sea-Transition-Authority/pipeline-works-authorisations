package uk.co.ogauthority.pwa.model.entity.publicnotice;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

@Entity
@Table(name = "public_notice_dates")
public class PublicNoticeDates {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "public_notice_id")
  @ManyToOne
  private PublicNotice publicNotice;

  private Instant publicationStartTimestamp;
  private Instant publicationEndTimestamp;

  private Integer createdByPersonId;
  private Integer endedByPersonId;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PublicNotice getPublicNotice() {
    return publicNotice;
  }

  public void setPublicNotice(PublicNotice publicNotice) {
    this.publicNotice = publicNotice;
  }

  public Instant getPublicationStartTimestamp() {
    return publicationStartTimestamp;
  }

  public void setPublicationStartTimestamp(Instant publicationStartTimestamp) {
    this.publicationStartTimestamp = publicationStartTimestamp;
  }

  public Instant getPublicationEndTimestamp() {
    return publicationEndTimestamp;
  }

  public void setPublicationEndTimestamp(Instant publicationEndTimestamp) {
    this.publicationEndTimestamp = publicationEndTimestamp;
  }

  public Integer getCreatedByPersonId() {
    return createdByPersonId;
  }

  public void setCreatedByPersonId(Integer createdByPersonId) {
    this.createdByPersonId = createdByPersonId;
  }

  public Integer getEndedByPersonId() {
    return endedByPersonId;
  }

  public void setEndedByPersonId(Integer endedByPersonId) {
    this.endedByPersonId = endedByPersonId;
  }
}
