package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReview;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

@Entity
@Table(name = "parallel_consent_check_log")
public class ParallelConsentCheckLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer padConsentReviewId;
  private Integer pwaConsentId;
  private Integer checkedByPersonId;
  private Instant checkConfirmedTimestamp;


  public ParallelConsentCheckLog(ConsentReview consentReview,
                                 Integer pwaConsentId,
                                 Person checkedByPerson,
                                 Instant checkConfirmedTimestamp) {
    this.padConsentReviewId = consentReview.getId();
    this.pwaConsentId = pwaConsentId;
    this.checkedByPersonId = checkedByPerson.getId().asInt();
    this.checkConfirmedTimestamp = checkConfirmedTimestamp;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public Integer getPadConsentReviewId() {
    return padConsentReviewId;
  }

  public void setPadConsentReviewId(Integer padConsentReviewId) {
    this.padConsentReviewId = padConsentReviewId;
  }


  public Integer getPwaConsentId() {
    return pwaConsentId;
  }

  public void setPwaConsentId(Integer pwaConsentId) {
    this.pwaConsentId = pwaConsentId;
  }


  public Integer getCheckedByPersonId() {
    return checkedByPersonId;
  }

  public void setCheckedByPersonId(Integer checkedByPersonId) {
    this.checkedByPersonId = checkedByPersonId;
  }


  public Instant getCheckConfirmedTimestamp() {
    return checkConfirmedTimestamp;
  }

  public void setCheckConfirmedTimestamp(Instant checkConfirmedTimestamp) {
    this.checkConfirmedTimestamp = checkConfirmedTimestamp;
  }

}
