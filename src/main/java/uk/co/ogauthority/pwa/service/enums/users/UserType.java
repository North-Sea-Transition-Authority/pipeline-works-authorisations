package uk.co.ogauthority.pwa.service.enums.users;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;

/**
 * Used to distinguish between the different groups of users on the system.
 */
public enum UserType {

  INDUSTRY(80, PwaUserPrivilege.PWA_INDUSTRY),

  OGA(100, PwaUserPrivilege.PWA_REGULATOR),

  CONSULTEE(60, PwaUserPrivilege.PWA_CONSULTEE);

  /**
   * Higher priority user types tend to open up more permissions within the application.
   */
  private final int priority;

  private final PwaUserPrivilege qualifyingPrivilege;

  UserType(int priority, PwaUserPrivilege qualifyingPrivilege) {
    this.priority = priority;
    this.qualifyingPrivilege = qualifyingPrivilege;
  }

  /**
   * Higher priority user types have larger int values.
   */
  public int getPriority() {
    return priority;
  }

  public PwaUserPrivilege getQualifyingPrivilege() {
    return qualifyingPrivilege;
  }

  public static Stream<UserType> stream() {
    return Arrays.stream(UserType.values());
  }
}
