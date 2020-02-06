package uk.co.ogauthority.pwa.energyportal.model.dto.teams;

public class PortalTeamDto {
  private final int resId;

  private final String name;

  private final String description;

  private final String type;

  private final PortalTeamScopeDto scope;

  public PortalTeamDto(int resId, String teamName, String teamDescription, String teamType, String primaryTeamUsage) {
    this.resId = resId;
    this.name = teamName;
    this.description = teamDescription;
    this.type = teamType;
    this.scope = new PortalTeamScopeDto(primaryTeamUsage);
  }

  public int getResId() {
    return resId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getType() {
    return type;
  }

  public PortalTeamScopeDto getScope() {
    return scope;
  }

  @Override
  public String toString() {
    return "PortalTeamDto{" +
        "resId=" + resId +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", type='" + type + '\'' +
        ", scope=" + scope.getPrimaryScope() +
        "}";
  }
}
