package uk.co.ogauthority.pwa.energyportal.logout;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.service.EnergyPortalUrlService;

@Component
public class ServiceLogoutSuccessHandler implements LogoutSuccessHandler {

  private final EnergyPortalUrlService energyPortalUrlService;

  @Autowired
  public ServiceLogoutSuccessHandler(EnergyPortalUrlService energyPortalUrlService) {
    this.energyPortalUrlService = energyPortalUrlService;
  }

  @Override
  public void onLogoutSuccess(HttpServletRequest request,
                              HttpServletResponse response,
                              Authentication authentication) throws IOException {
    response.sendRedirect(energyPortalUrlService.getLogoutUrl());
  }
}