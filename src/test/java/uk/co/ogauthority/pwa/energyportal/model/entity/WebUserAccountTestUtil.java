package uk.co.ogauthority.pwa.energyportal.model.entity;

import uk.co.ogauthority.pwa.energyportal.model.WebUserAccountStatus;

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