package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument;

import jakarta.persistence.Basic;
import jakarta.persistence.Convert;
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
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity
@Table(name = "pad_consent_reviews")
public class ConsentReview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pad_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @Enumerated(EnumType.STRING)
  private ConsentReviewStatus status;

  private String coverLetterText;

  @Basic // this annotation allows the Jpa metamodel to pick up the field, but leaves default behaviour intact.
  // Suitable as PersonId just wraps a basic class.
  @Convert(converter = PersonIdConverter.class)
  private PersonId startedByPersonId;

  private Instant startTimestamp;

  @Basic // this annotation allows the Jpa metamodel to pick up the field, but leaves default behaviour intact.
  // Suitable as PersonId just wraps a basic class.
  @Convert(converter = PersonIdConverter.class)
  private PersonId endedByPersonId;

  private Instant endTimestamp;

  private String endedReason;

  public ConsentReview() {

  }

  public ConsentReview(PwaApplicationDetail pwaApplicationDetail,
                       String coverLetterText,
                       PersonId startedByPersonId,
                       Instant startTimestamp) {
    this.pwaApplicationDetail = pwaApplicationDetail;
    this.status = ConsentReviewStatus.OPEN;
    this.coverLetterText = coverLetterText;
    this.startedByPersonId = startedByPersonId;
    this.startTimestamp = startTimestamp;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public ConsentReviewStatus getStatus() {
    return status;
  }

  public void setStatus(ConsentReviewStatus status) {
    this.status = status;
  }

  public String getCoverLetterText() {
    return coverLetterText;
  }

  public void setCoverLetterText(String coverLetterText) {
    this.coverLetterText = coverLetterText;
  }

  public PersonId getStartedByPersonId() {
    return startedByPersonId;
  }

  public void setStartedByPersonId(PersonId startedByPersonId) {
    this.startedByPersonId = startedByPersonId;
  }

  public Instant getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(Instant startTimestamp) {
    this.startTimestamp = startTimestamp;
  }

  public PersonId getEndedByPersonId() {
    return endedByPersonId;
  }

  public void setEndedByPersonId(PersonId endedByPersonId) {
    this.endedByPersonId = endedByPersonId;
  }

  public Instant getEndTimestamp() {
    return endTimestamp;
  }

  public void setEndTimestamp(Instant endTimestamp) {
    this.endTimestamp = endTimestamp;
  }

  public String getEndedReason() {
    return endedReason;
  }

  public void setEndedReason(String endedReason) {
    this.endedReason = endedReason;
  }
}
