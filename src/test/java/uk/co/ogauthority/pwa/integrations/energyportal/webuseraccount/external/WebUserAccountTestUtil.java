package uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external;

import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

public final class WebUserAccountTestUtil {


  public static WebUserAccount createWebUserAccountMatchingPerson(Integer id,
                                                                  Person person,
                                                                  WebUserAccountStatus userAccountStatus) {
    return new WebUserAccount(
        id, person.getEmailAddress(), person.getEmailAddress(), userAccountStatus, person);
  }

  public static WebUserAccount createWebUserAccount(Integer id,
                                                    Person person,
                                                    String loginId,
                                                    WebUserAccountStatus userAccountStatus) {
    return new WebUserAccount(
        id, person.getEmailAddress(), loginId, userAccountStatus, person);
  }


}