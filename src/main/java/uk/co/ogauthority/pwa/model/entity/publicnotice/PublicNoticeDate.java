package uk.co.ogauthority.pwa.model.entity.publicnotice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import uk.co.ogauthority.pwa.util.DateUtils;

@Entity
@Table(name = "public_notice_dates")
public class PublicNoticeDate {


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

  private Instant createdTimestamp;
  private Instant endedTimestamp;
  private String dateChangeReason;

  public PublicNoticeDate() {
    //default
  }

  public PublicNoticeDate(PublicNotice publicNotice, Instant publicationStartTimestamp,
                          Instant publicationEndTimestamp, Integer createdByPersonId,
                          Instant createdTimestamp) {
    this.publicNotice = publicNotice;
    this.publicationStartTimestamp = publicationStartTimestamp;
    this.publicationEndTimestamp = publicationEndTimestamp;
    this.createdByPersonId = createdByPersonId;
    this.createdTimestamp = createdTimestamp;
  }

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

  public long getPublicationDaysLength() {
    return ChronoUnit.DAYS.between(DateUtils.instantToLocalDate(publicationStartTimestamp),
        DateUtils.instantToLocalDate(publicationEndTimestamp));
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

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  public Instant getEndedTimestamp() {
    return endedTimestamp;
  }

  public void setEndedTimestamp(Instant endedTimestamp) {
    this.endedTimestamp = endedTimestamp;
  }

  public String getDateChangeReason() {
    return dateChangeReason;
  }

  public PublicNoticeDate setDateChangeReason(String dateChangeReason) {
    this.dateChangeReason = dateChangeReason;
    return this;
  }
}
