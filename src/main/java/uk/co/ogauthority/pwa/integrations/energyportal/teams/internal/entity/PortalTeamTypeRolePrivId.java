package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PortalTeamTypeRolePrivId implements Serializable {

  @Column(name = "role_name")
  private String roleName;

  @Column(name = "res_type")
  private String resType;

  @Column(name = "default_system_priv")
  private String privilege;

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public String getResType() {
    return resType;
  }

  public void setResType(String resType) {
    this.resType = resType;
  }

  public String getPrivilege() {
    return privilege;
  }

  public void setPrivilege(String privilege) {
    this.privilege = privilege;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PortalTeamTypeRolePrivId)) {
      return false;
    }
    PortalTeamTypeRolePrivId that = (PortalTeamTypeRolePrivId) o;
    return Objects.equals(roleName, that.roleName)
        && Objects.equals(resType, that.resType)
        && Objects.equals(privilege, that.privilege);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleName, resType, privilege);
  }
}
