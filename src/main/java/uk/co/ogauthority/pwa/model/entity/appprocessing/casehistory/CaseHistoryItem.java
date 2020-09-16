package uk.co.ogauthority.pwa.model.entity.appprocessing.casehistory;

import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.model.entity.enums.appprocessing.casehistory.CaseHistoryItemType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

/**
 * MappedSuperclass for case history items containing common fields
 * Classes extending this can add their own custom fields if necessary.
 */
@MappedSuperclass
public class CaseHistoryItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pwa_application_id")
  private PwaApplication pwaApplication;

  @Transient
  private CaseHistoryItemType itemType;

  @Basic // this annotation allows the Jpa metamodel to pick up the field, but leaves default behaviour intact.
  // Suitable as PersonId just wraps a basic class.
  @Column(name = "person_id")
  @Convert(converter = PersonIdConverter.class)
  private PersonId personId;

  @Column(name = "date_time")
  private Instant dateTime;

  public CaseHistoryItem() {
  }

  public CaseHistoryItem(PwaApplication pwaApplication,
                         CaseHistoryItemType itemType,
                         PersonId personId,
                         Instant dateTime) {
    this.pwaApplication = pwaApplication;
    this.itemType = itemType;
    this.personId = personId;
    this.dateTime = dateTime;
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

  public CaseHistoryItemType getItemType() {
    return itemType;
  }

  public void setItemType(CaseHistoryItemType itemType) {
    this.itemType = itemType;
  }

  public PersonId getPersonId() {
    return personId;
  }

  public void setPersonId(PersonId personId) {
    this.personId = personId;
  }

  public Instant getDateTime() {
    return dateTime;
  }

  public void setDateTime(Instant dateTime) {
    this.dateTime = dateTime;
  }
}
