package uk.co.ogauthority.pwa.service.workarea.viewentities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import uk.co.ogauthority.pwa.service.workarea.ApplicationLifecycleEvent;

@Embeddable
public final class EmbeddedWorkAreaApplicationLifecycleEventId implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(name = "pwa_application_id")
  private Integer pwaApplicationId;

  @Column(name = "flag")
  @Enumerated(EnumType.STRING)
  private ApplicationLifecycleEvent eventFlag;

  public Integer getPwaApplicationId() {
    return pwaApplicationId;
  }

  public ApplicationLifecycleEvent getEventFlag() {
    return eventFlag;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EmbeddedWorkAreaApplicationLifecycleEventId that = (EmbeddedWorkAreaApplicationLifecycleEventId) o;
    return Objects.equals(pwaApplicationId, that.pwaApplicationId) && eventFlag == that.eventFlag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(pwaApplicationId, eventFlag);
  }
}
