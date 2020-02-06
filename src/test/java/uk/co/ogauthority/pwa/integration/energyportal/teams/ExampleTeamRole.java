package uk.co.ogauthority.pwa.integration.energyportal.teams;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public enum ExampleTeamRole {
  ROLE_WITH_PRIV("PrivName", "WITH_PRIV_TITLE", "WITH_PRIV_DESC", 0, 999),
  ROLE_WITHOUT_PRIV(null, "NO_PRIV_TITLE", "NO_PRIV_DESC", 1, 999);

  private final String exampleRolePriv;
  private final String title;
  private final String desc;
  private final int minMembers;
  private final int maxMembers;

  ExampleTeamRole(String exampleRolePriv, String title, String desc, int minMembers, int maxMembers) {
    this.exampleRolePriv = exampleRolePriv;
    this.title = title;
    this.desc = desc;
    this.minMembers = minMembers;
    this.maxMembers = maxMembers;
  }

  public static Collection<String> getAllRoleNames(){
    return Arrays.stream(ExampleTeamRole.values())
        .map(Enum::name)
        .collect(Collectors.toList());
  }

  public String getExampleRolePriv() {
    return exampleRolePriv;
  }

  public int getMinMembers() {
    return minMembers;
  }

  public int getMaxMembers() {
    return maxMembers;
  }

  public String getTitle() {
    return title;
  }

  public String getDesc() {
    return desc;
  }
}

