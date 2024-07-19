package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PortalTeamUsageId implements Serializable {

  @Column(name = "res_id")
  private String resId;

  @Column(name = "uref")
  private String uref;

  @Column(name = "purpose")
  private String purpose;

  public String getResId() {
    return resId;
  }

  public String getUref() {
    return uref;
  }

  public String getPurpose() {
    return purpose;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PortalTeamUsageId)) {
      return false;
    }
    PortalTeamUsageId that = (PortalTeamUsageId) o;
    return Objects.equals(resId, that.resId)
        && Objects.equals(uref, that.uref);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resId, uref);
  }
}
