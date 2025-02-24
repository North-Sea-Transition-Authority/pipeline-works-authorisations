package uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external;

import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

public final class WebUserAccountTestUtil {

  public static WebUserAccount createWebUserAccountMatchingPerson(Integer id,
                                                                  Person person,
                                                                  WebUserAccountStatus userAccountStatus) {
    WebUserAccount wua = new WebUserAccount();
    wua.setWuaId(id);
    wua.setEmailAddress(person.getEmailAddress());
    wua.setLoginId(person.getEmailAddress());
    wua.setAccountStatus(userAccountStatus);
    wua.setPerson(person);
    return wua;
  }

  public static WebUserAccount createWebUserAccount(Integer id,
                                                    Person person,
                                                    String loginId,
                                                    WebUserAccountStatus userAccountStatus) {
    WebUserAccount wua = new WebUserAccount();
    wua.setWuaId(id);
    wua.setEmailAddress(person.getEmailAddress());
    wua.setLoginId(loginId);
    wua.setAccountStatus(userAccountStatus);
    wua.setPerson(person);
    return wua;
  }

}