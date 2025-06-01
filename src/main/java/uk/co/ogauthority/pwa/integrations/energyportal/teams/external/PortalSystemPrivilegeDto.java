package uk.co.ogauthority.pwa.integrations.energyportal.teams.external;

import java.util.Objects;

// TODO: Remove in PWARE-60
public class PortalSystemPrivilegeDto {

  private final String portalTeamType;
  private final String roleName;
  private final String grantedPrivilege;

  public PortalSystemPrivilegeDto(String portalTeamType, String roleName, String grantedPrivilege) {
    this.portalTeamType = portalTeamType;
    this.roleName = roleName;
    this.grantedPrivilege = grantedPrivilege;
  }

  public String getPortalTeamType() {
    return portalTeamType;
  }

  public String getRoleName() {
    return roleName;
  }

  public String getGrantedPrivilege() {
    return grantedPrivilege;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PortalSystemPrivilegeDto)) {
      return false;
    }
    PortalSystemPrivilegeDto that = (PortalSystemPrivilegeDto) o;
    return portalTeamType.equals(that.portalTeamType)
        && roleName.equals(that.roleName)
        && grantedPrivilege.equals(that.grantedPrivilege);
  }

  @Override
  public int hashCode() {
    return Objects.hash(portalTeamType, roleName, grantedPrivilege);
  }
}
