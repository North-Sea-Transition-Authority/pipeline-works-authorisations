package uk.co.ogauthority.pwa.model.entity.appprocessing.casehistory;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.time.Instant;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.model.entity.enums.appprocessing.casehistory.CaseHistoryItemType;

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
