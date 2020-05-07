package uk.co.ogauthority.pwa.auth;

import java.io.Serializable;
import java.util.Collection;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;

/**
 * An AuthenticatedUserAccount represents a WebUserAccount which is currently in an authenticated state.
 * This entity is the Spring Security Principal.
 */
public class AuthenticatedUserAccount extends WebUserAccount implements Serializable {

  private static final long serialVersionUID = 1;

  private Collection<PwaUserPrivilege> userPrivileges;

  public AuthenticatedUserAccount(WebUserAccount webUserAccount, Collection<PwaUserPrivilege> userPrivileges) {
    this.wuaId = webUserAccount.getWuaId();
    this.title = webUserAccount.getTitle();
    this.forename = webUserAccount.getForename();
    this.surname = webUserAccount.getSurname();
    this.emailAddress = webUserAccount.getEmailAddress();
    this.loginId = webUserAccount.getLoginId();
    this.accountStatus = webUserAccount.getAccountStatus();
    this.person = webUserAccount.getLinkedPerson();
    this.userPrivileges = userPrivileges;
  }

  public Collection<PwaUserPrivilege> getUserPrivileges() {
    return userPrivileges;
  }

  public boolean hasPrivilege(PwaUserPrivilege pwaUserPrivilege) {
    return userPrivileges.contains(pwaUserPrivilege);
  }
}
