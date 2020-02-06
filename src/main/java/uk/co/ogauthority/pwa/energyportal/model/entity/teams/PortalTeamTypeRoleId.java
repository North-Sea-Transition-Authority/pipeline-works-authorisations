package uk.co.ogauthority.pwa.energyportal.model.entity.teams;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
class PortalTeamTypeRoleId implements Serializable {

  @Column(name = "res_type")
  private String resType;

  @Column(name = "role_name")
  private String roleName;

  public String getResType() {
    return resType;
  }

  public String getRoleName() {
    return roleName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PortalTeamTypeRoleId)) {
      return false;
    }
    PortalTeamTypeRoleId that = (PortalTeamTypeRoleId) o;
    return Objects.equals(resType, that.resType)
        && Objects.equals(roleName, that.roleName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resType, roleName);
  }
}
