package uk.co.ogauthority.pwa.testutils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;

public class AuthTestingUtils {

  /**
   * Check that the given testFunction returns true only when at least one of the requiredPrivs is passed to it, otherwise it should return false.
   */
  public static void testPrivilegeBasedAuthenticationFunction(Set<PwaUserPrivilege> requiredPrivs, Function<AuthenticatedUserAccount, Boolean> testFunction) {

    // No privs
    assertThat(testFunction.apply(new AuthenticatedUserAccount(new WebUserAccount(1), List.of())))
        .isEqualTo(false);

    // Single priv
    for (PwaUserPrivilege privilege : PwaUserPrivilege.values()) {
      boolean expectedResult = requiredPrivs.contains(privilege);

      try {
        assertThat(testFunction.apply(new AuthenticatedUserAccount(new WebUserAccount(1), List.of(privilege))))
            .isEqualTo(expectedResult);
      } catch (AssertionError e) {
        throw new AssertionError(String.format("Priv check function returned %s with priv %s. Expected %s", !expectedResult, privilege, expectedResult), e);
      }
    }

    // List of pivs
    for (PwaUserPrivilege priv1 : PwaUserPrivilege.values()) {
      for (PwaUserPrivilege priv2 : PwaUserPrivilege.values()) {
        boolean expectedResult = requiredPrivs.contains(priv1) || requiredPrivs.contains(priv2);
        try {
          assertThat(testFunction.apply(new AuthenticatedUserAccount(new WebUserAccount(1), List.of(priv1, priv2))))
              .isEqualTo(expectedResult);
        } catch (AssertionError e) {
          throw new AssertionError(String.format("Priv check function returned %s with privs %s. Expected %s", !expectedResult, List.of(priv1, priv2), expectedResult), e);
        }
      }
    }
  }

}
