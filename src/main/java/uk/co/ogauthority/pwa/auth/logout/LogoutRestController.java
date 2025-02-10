package uk.co.ogauthority.pwa.auth.logout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ogauthority.pwa.auth.EnergyPortalConfiguration;
import uk.co.ogauthority.pwa.auth.InvalidAuthenticationException;

@RestController
@RequestMapping("/api/v1/logout/")
public class LogoutRestController {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogoutRestController.class);
  private static final String AUTH_HEADER_BEARER_PREFIX = "Bearer ";

  private final EnergyPortalConfiguration energyPortalConfiguration;
  private final LogoutService logoutService;

  @Autowired
  LogoutRestController(EnergyPortalConfiguration energyPortalConfiguration,
                       LogoutService logoutService) {
    this.energyPortalConfiguration = energyPortalConfiguration;
    this.logoutService = logoutService;
  }

  /**
   * Logs the specified user out from the service, if the pre-shared key matches the one found in the
   * authorization header.
   * @param apiKey the pre-shared key used to authorize the request
   * @param wuaId the web user account id of the user to be logged out the service
   * @return a http response with the code 200 if successful
   */
  @PostMapping("{wuaId}")
  ResponseEntity<Object> logoutOfService(@RequestHeader("Authorization") String apiKey,
                                         @PathVariable("wuaId") Long wuaId) {
    var expectedKey = energyPortalConfiguration.portalLogoutPreSharedKey();
    if (apiKey.startsWith(AUTH_HEADER_BEARER_PREFIX) && expectedKey.equals(apiKey.replace(AUTH_HEADER_BEARER_PREFIX, ""))) {
      logoutService.logoutUser(wuaId);
      LOGGER.info("Logout request received from energy portal for wuaId: {}. User logged out.", wuaId);
      return ResponseEntity.ok().build();
    } else {
      var errorMessage = "Attempted to logout of service, invalid key used";
      LOGGER.warn(errorMessage);
      throw new InvalidAuthenticationException(errorMessage);
    }
  }
}
