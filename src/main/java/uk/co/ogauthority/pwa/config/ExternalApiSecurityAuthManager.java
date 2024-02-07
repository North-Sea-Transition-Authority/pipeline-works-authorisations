package uk.co.ogauthority.pwa.config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class ExternalApiSecurityAuthManager implements AuthenticationManager {

  private final String expectedKey;

  public ExternalApiSecurityAuthManager(String expectedKey) {
    this.expectedKey = expectedKey;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String providedKey = (String) authentication.getPrincipal();
    authentication.setAuthenticated(false);
    if (expectedKey.equals(providedKey)) {
      authentication.setAuthenticated(true);
    } else if (providedKey == null || providedKey.isEmpty()) {
      throw new BadCredentialsException("The Authorization key was missing.");
    } else {
      throw new BadCredentialsException("The Authorization key was not the expected value.");
    }
    return authentication;
  }
}
