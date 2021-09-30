package uk.co.ogauthority.pwa.auth;

import static java.util.stream.Collectors.toSet;

import java.util.Set;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AuthenticatedUserToken extends AbstractAuthenticationToken {

  private final String sessionId;
  private final AuthenticatedUserAccount principal;

  public static AuthenticatedUserToken create(String sessionId, AuthenticatedUserAccount principal) {

    Set<GrantedAuthority> grantedAuthorities = principal.getUserPrivileges().stream()
        .map(PwaUserPrivilege::name)
        .map(SimpleGrantedAuthority::new)
        .collect(toSet());

    return new AuthenticatedUserToken(sessionId, principal, grantedAuthorities);

  }

  private AuthenticatedUserToken(String sessionId, AuthenticatedUserAccount principal,
                                 Set<GrantedAuthority> grantedAuthorities) {
    super(grantedAuthorities);
    this.sessionId = sessionId;
    this.principal = principal;
    this.setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return getSessionId();
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  public String getSessionId() {
    return sessionId;
  }

}

