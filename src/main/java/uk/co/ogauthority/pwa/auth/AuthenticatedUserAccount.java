package uk.co.ogauthority.pwa.auth;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;

/**
 * An AuthenticatedUserAccount represents a WebUserAccount which is currently in an authenticated state.
 * This entity is the Spring Security Principal.
 */
public class AuthenticatedUserAccount extends WebUserAccount implements Serializable, UserDetails {

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


  public void setPrivileges(Collection<PwaUserPrivilege> userPrivileges) {
    this.userPrivileges = userPrivileges;
  }

  /**
   * Return user privileges as GrantedAuthorities, so they can be matched in Spring HttpSecurity rules.
   * @return Users privs only (not org privs) as a collection of GrantedAuthorities
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return userPrivileges.stream()
        .map(p -> new SimpleGrantedAuthority(p.name()))
        .collect(Collectors.toList());
  }


  public boolean hasPrivilege(PwaUserPrivilege pwaUserPrivilege) {
    return userPrivileges.contains(pwaUserPrivilege);
  }




  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return getEmailAddress();
  }

  @Override
  public boolean isAccountNonExpired() {
    return false;
  }

  @Override
  public boolean isAccountNonLocked() {
    return false;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
