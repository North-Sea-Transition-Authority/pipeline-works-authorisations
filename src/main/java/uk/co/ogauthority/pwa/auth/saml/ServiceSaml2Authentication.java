package uk.co.ogauthority.pwa.auth.saml;

import java.io.Serial;
import java.util.Collection;
import java.util.Objects;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;

class ServiceSaml2Authentication extends AbstractAuthenticationToken {

  @Serial
  private static final long serialVersionUID = 4671397789349743401L;
  private final AuthenticatedUserAccount principal;

  ServiceSaml2Authentication(AuthenticatedUserAccount principal, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ServiceSaml2Authentication that = (ServiceSaml2Authentication) o;
    return principal.equals(that.principal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), principal);
  }
}
