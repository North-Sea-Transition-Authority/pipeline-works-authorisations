package uk.co.ogauthority.pwa.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class ServiceLogoutSuccessHandler implements LogoutSuccessHandler {

  private final EnergyPortalConfiguration energyPortalConfiguration;

  @Autowired
  public ServiceLogoutSuccessHandler(EnergyPortalConfiguration energyPortalConfiguration) {
    this.energyPortalConfiguration = energyPortalConfiguration;
  }

  @Override
  public void onLogoutSuccess(HttpServletRequest request,
                              HttpServletResponse response,
                              Authentication authentication) throws IOException {
    response.sendRedirect(energyPortalConfiguration.logoutUrl());
  }
}
