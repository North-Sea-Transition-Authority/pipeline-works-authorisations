package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PortalTeamMemberId implements Serializable {

  @Column(name = "person_id")
  private int personId;

  @Column(name = "res_id")
  private int resId;

  public int getPersonId() {
    return personId;
  }

  public int getResId() {
    return resId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PortalTeamMemberId)) {
      return false;
    }
    PortalTeamMemberId that = (PortalTeamMemberId) o;
    return personId == that.personId
        && resId == that.resId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(personId, resId);
  }
}
