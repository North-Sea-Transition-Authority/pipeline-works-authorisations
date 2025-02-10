package uk.co.ogauthority.pwa.util;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.auth.saml.SamlAuthenticationUtil;

public class TestUserProvider {

  public static RequestPostProcessor user(AuthenticatedUserAccount authenticatedUser) {
    Set<GrantedAuthority> grantedAuthorities = authenticatedUser.getUserPrivileges().stream()
        .map(PwaUserPrivilege::name)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());

    var authentication = SamlAuthenticationUtil.Builder()
        .withUser(authenticatedUser)
        .withGrantedAuthorities(grantedAuthorities)
        .build();

    return authentication(authentication);
  }
}