package uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent;

import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.prepareconsent.ConsentReviewStatus;

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
