package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;

@Entity
@Table(name = "pwa_app_charge_requests")
public class PwaAppChargeRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", name = "pwa_application_id")
  private PwaApplication pwaApplication;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "requested_by_person_id")
  private PersonId requestedByPersonId;
  private Instant requestedByTimestamp;


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


  public PersonId getRequestedByPersonId() {
    return requestedByPersonId;
  }

  public void setRequestedByPersonId(PersonId requestedByPersonId) {
    this.requestedByPersonId = requestedByPersonId;
  }


  public Instant getRequestedByTimestamp() {
    return requestedByTimestamp;
  }

  public void setRequestedByTimestamp(Instant requestedByTimestamp) {
    this.requestedByTimestamp = requestedByTimestamp;
  }

}
