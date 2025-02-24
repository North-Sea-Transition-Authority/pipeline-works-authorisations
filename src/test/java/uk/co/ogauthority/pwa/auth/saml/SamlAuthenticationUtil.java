package uk.co.ogauthority.pwa.auth.saml;

import java.util.Collection;
import java.util.HashSet;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;

public class SamlAuthenticationUtil {

  public SamlAuthenticationUtil() {
    throw new AssertionError(this.getClass());
  }

  public static Builder Builder() {
    return new Builder();
  }

  public static class Builder {

    private Builder() {
    }

    private AuthenticatedUserAccount authenticatedUserAccount = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();
    private final Collection<GrantedAuthority> grantedAuthorities = new HashSet<>();

    public void setSecurityContext() {
      var authentication = build();
      SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
    }

    public ServiceSaml2Authentication build() {
      return new ServiceSaml2Authentication(authenticatedUserAccount, grantedAuthorities);
    }

    public Builder withGrantedAuthorities(Collection<GrantedAuthority> grantedAuthorities) {
      this.grantedAuthorities.addAll(grantedAuthorities);
      return this;
    }

    public Builder withGrantedAuthority(GrantedAuthority grantedAuthority) {
      this.grantedAuthorities.add(grantedAuthority);
      return this;
    }

    public Builder withGrantedAuthority(String grantedAuthority) {
      this.grantedAuthorities.add(new SimpleGrantedAuthority(grantedAuthority));
      return this;
    }

    public Builder withUser(AuthenticatedUserAccount authenticatedUserAccount) {
      this.authenticatedUserAccount = authenticatedUserAccount;
      return this;
    }
  }
}