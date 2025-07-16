package uk.co.ogauthority.pwa.auth;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;

public class AuthenticatedUserAccountTest {


  @Test
  void displayNameIncludingAnyProxyUser_whenProxyUser_thenIncludeProxyUsername() {
    var forename = "forename";
    var surname = "surname";
    var proxyWuaId = 999;
    var proxyUsername = "proxyUsername";

    var authenticatedUserAccount = new AuthenticatedUserAccount(
        new WebUserAccount(
            1,
            PersonTestUtil.createPersonFrom(new PersonId(2))
        ),
        EnumSet.allOf(PwaUserPrivilege.class),
        proxyWuaId,
        proxyUsername
    );
    authenticatedUserAccount.setForename(forename);
    authenticatedUserAccount.setSurname(surname);

    var result = authenticatedUserAccount.displayNameIncludingAnyProxyUser();

    assertThat(result).isEqualTo(String.format("%s as %s %s", proxyUsername, forename, surname));
  }

  @Test
  void displayNameIncludingAnyProxyUser_whenNotProxyUser_theJustUsersName() {
    var forename = "forename";
    var surname = "surname";

    var authenticatedUserAccount = new AuthenticatedUserAccount(
        new WebUserAccount(
            1,
            PersonTestUtil.createPersonFrom(new PersonId(2))
        ),
        EnumSet.allOf(PwaUserPrivilege.class)
    );
    authenticatedUserAccount.setForename(forename);
    authenticatedUserAccount.setSurname(surname);

    var result = authenticatedUserAccount.displayNameIncludingAnyProxyUser();

    assertThat(result).isEqualTo(String.format("%s %s", forename, surname));
  }
}
