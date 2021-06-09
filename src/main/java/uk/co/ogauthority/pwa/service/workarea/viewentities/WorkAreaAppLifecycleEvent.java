package uk.co.ogauthority.pwa.service.workarea.viewentities;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.workarea.ApplicationLifecycleEvent;

@Entity
@Table(name = "workarea_app_lifecycle_events")
@Immutable
public class WorkAreaAppLifecycleEvent {

  @Id
  @EmbeddedId
  private EmbeddedWorkAreaApplicationLifecycleEventId id;

  @ManyToOne
  @JoinColumn(name = "pwa_application_id", insertable = false, updatable = false)
  private PwaApplication pwaApplication;

  @Column(name = "pwa_application_id", insertable = false, updatable = false)
  private Integer pwaApplicationId;

  @Column(name = "flag", insertable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private ApplicationLifecycleEvent eventFlag;

  public EmbeddedWorkAreaApplicationLifecycleEventId getId() {
    return id;
  }

  public void setId(EmbeddedWorkAreaApplicationLifecycleEventId id) {
    this.id = id;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }

  public void setPwaApplication(PwaApplication pwaApplicationId) {
    this.pwaApplication = pwaApplicationId;
  }

  public ApplicationLifecycleEvent getEventFlag() {
    return eventFlag;
  }

  public void setEventFlag(ApplicationLifecycleEvent eventFlag) {
    this.eventFlag = eventFlag;
  }

  public Integer getPwaApplicationId() {
    return pwaApplicationId;
  }
}
