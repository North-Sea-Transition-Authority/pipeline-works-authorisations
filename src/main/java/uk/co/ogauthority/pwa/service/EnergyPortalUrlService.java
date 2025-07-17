package uk.co.ogauthority.pwa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EnergyPortalUrlService {

  private final String loginUrl;
  private final String logoutUrl;
  private final String registrationUrl;

  public EnergyPortalUrlService(@Value("${energy-portal.login-url}") String loginUrl,
                                @Value("${energy-portal.logout-url}") String logoutUrl,
                                @Value("${energy-portal.registration-url}") String registrationUrl) {
    this.loginUrl = loginUrl;
    this.logoutUrl = logoutUrl;
    this.registrationUrl = registrationUrl;
  }

  public String getLoginUrl() {
    return loginUrl;
  }

  public String getLogoutUrl() {
    return logoutUrl;
  }

  public String getRegistrationUrl() {
    return registrationUrl;
  }
}
