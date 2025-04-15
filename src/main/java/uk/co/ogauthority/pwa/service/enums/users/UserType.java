package uk.co.ogauthority.pwa.service.enums.users;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.teams.TeamType;

/**
 * Used to distinguish between the different groups of users on the system.
 */
public enum UserType {

  INDUSTRY(80, TeamType.ORGANISATION),

  OGA(100, TeamType.REGULATOR),

  CONSULTEE(60, TeamType.CONSULTEE);

  /**
   * Higher priority user types tend to open up more permissions within the application.
   */
  private final int priority;

  private final TeamType teamType;

  UserType(int priority, TeamType teamType) {
    this.priority = priority;
    this.teamType = teamType;
  }

  /**
   * Higher priority user types have larger int values.
   */
  public int getPriority() {
    return priority;
  }

  public TeamType getTeamType() {
    return teamType;
  }

  public static Stream<UserType> stream() {
    return Arrays.stream(UserType.values());
  }
}
