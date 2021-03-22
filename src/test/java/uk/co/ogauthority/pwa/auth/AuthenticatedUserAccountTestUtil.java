package uk.co.ogauthority.pwa.auth;

import java.util.EnumSet;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;

public final class AuthenticatedUserAccountTestUtil {
  private static final int WUA_ID = 1;
  private static final PersonId PERSON_ID = new PersonId(2);

  public static AuthenticatedUserAccount defaultAllPrivUserAccount(){
    return new AuthenticatedUserAccount(
        new WebUserAccount(
            WUA_ID,
            PersonTestUtil.createPersonFrom(PERSON_ID)
        ),
        EnumSet.allOf(PwaUserPrivilege.class)
    );

  }

}