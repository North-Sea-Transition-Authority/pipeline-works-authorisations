package uk.co.ogauthority.pwa.auth;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.core.AuthenticatedPrincipal;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;

/**
 * An AuthenticatedUserAccount represents a WebUserAccount which is currently in an authenticated state.
 * This entity is the Spring Security Principal.
 */
public class AuthenticatedUserAccount extends WebUserAccount implements AuthenticatedPrincipal, Serializable {

  private static final long serialVersionUID = 1;

  private Collection<PwaUserPrivilege> userPrivileges;
  private Integer proxyUserWuaId;

  public AuthenticatedUserAccount() {
  }

  public AuthenticatedUserAccount(WebUserAccount webUserAccount, Collection<PwaUserPrivilege> userPrivileges) {
    this(webUserAccount, userPrivileges, null);
  }

  public AuthenticatedUserAccount(WebUserAccount webUserAccount,
                                  Collection<PwaUserPrivilege> userPrivileges,
                                  Integer proxyUserWuaId) {
    this.wuaId = webUserAccount.getWuaId();
    this.title = webUserAccount.getTitle();
    this.forename = webUserAccount.getForename();
    this.surname = webUserAccount.getSurname();
    this.emailAddress = webUserAccount.getEmailAddress();
    this.loginId = webUserAccount.getLoginId();
    this.accountStatus = webUserAccount.getAccountStatus();
    this.person = webUserAccount.getLinkedPerson();
    this.userPrivileges = userPrivileges;
    this.proxyUserWuaId = proxyUserWuaId;
  }

  public Collection<PwaUserPrivilege> getUserPrivileges() {
    return userPrivileges;
  }

  public void setPrivileges(Collection<PwaUserPrivilege> userPrivileges) {
    this.userPrivileges = userPrivileges;
  }

  public void setProxyUserWuaId(Integer proxyUserWuaId) {
    this.proxyUserWuaId = proxyUserWuaId;
  }

  public Optional<Integer> getProxyUserWuaId() {
    return Optional.ofNullable(proxyUserWuaId);
  }

  public boolean hasPrivilege(PwaUserPrivilege pwaUserPrivilege) {
    return userPrivileges.contains(pwaUserPrivilege);
  }

  @Override
  public String getName() {
    return Objects.nonNull(proxyUserWuaId) ? proxyUserWuaId.toString() : String.valueOf(wuaId);
  }
}
