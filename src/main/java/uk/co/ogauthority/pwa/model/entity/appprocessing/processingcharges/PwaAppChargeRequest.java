package uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges;

import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

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

  public void setPwaApplication(PwaApplication pwaApplicationId) {
    this.pwaApplication = pwaApplicationId;
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
