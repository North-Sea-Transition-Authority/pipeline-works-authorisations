package uk.co.ogauthority.pwa.auth;

import java.util.EnumSet;
import java.util.Set;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;

public final class AuthenticatedUserAccountTestUtil {
  private static final int WUA_ID = 1;
  private static final PersonId PERSON_ID = new PersonId(2);

  public static AuthenticatedUserAccount defaultAllPrivUserAccount() {
    return new AuthenticatedUserAccount(
        new WebUserAccount(
            WUA_ID,
            PersonTestUtil.createPersonFrom(PERSON_ID)
        ),
        EnumSet.allOf(PwaUserPrivilege.class)
    );

  }

  public static AuthenticatedUserAccount createAllPrivUserAccount(Integer personId) {
    return new AuthenticatedUserAccount(
        new WebUserAccount(
            WUA_ID,
            PersonTestUtil.createPersonFrom(new PersonId(personId))
        ),
        EnumSet.allOf(PwaUserPrivilege.class)
    );
  }

  public static AuthenticatedUserAccount createAllPrivWebUserAccount(int wuaId, Person person) {
    return new AuthenticatedUserAccount(
        new WebUserAccount(
            wuaId,
            person
        ),
        EnumSet.allOf(PwaUserPrivilege.class)
    );
  }

  public static AuthenticatedUserAccount createNoPrivUserAccount(Integer personId) {
    return new AuthenticatedUserAccount(
        new WebUserAccount(
            WUA_ID,
            PersonTestUtil.createPersonFrom(new PersonId(personId))
        ),
        Set.of()
    );
  }

}