package uk.co.ogauthority.pwa.auth.logout;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.EnergyPortalConfiguration;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.IncludeEnergyPortalConfigurationProperties;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = LogoutRestController.class)
@ContextConfiguration(classes = LogoutRestController.class)
@IncludeEnergyPortalConfigurationProperties
public class LogoutRestControllerTest extends AbstractControllerTest {

  private static final Class<LogoutRestController> CONTROLLER = LogoutRestController.class;
  private static final String UNAUTHORIZED_KEY = "Bearer INVALID_KEY";
  private static final Long WUA_ID = 1L;

  @Autowired
  protected EnergyPortalConfiguration energyPortalConfiguration;

  @MockBean
  private LogoutService logoutService;

  @Test
  public void logoutService() throws Exception {
    mockMvc
        .perform(post(ReverseRouter.route(on(CONTROLLER).logoutOfService(null, WUA_ID)))
            .header("Authorization", "Bearer " + energyPortalConfiguration.portalLogoutPreSharedKey()))
        .andExpect(status().isOk());
    verify(logoutService).logoutUser(WUA_ID);
  }

  @Test
  public void logoutService_unauthorized() throws Exception {
    mockMvc
        .perform(post(ReverseRouter.route(on(CONTROLLER).logoutOfService(null, WUA_ID)))
            .header("Authorization", UNAUTHORIZED_KEY))
        .andExpect(status().isForbidden());
    verify(logoutService, never()).logoutUser(any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"INVALID_KEY", "test"})
  void logoutService_invalidKey(String preSharedKey) throws Exception {
    mockMvc
        .perform(post(ReverseRouter.route(on(CONTROLLER).logoutOfService(null, WUA_ID)))
            .header("Authorization", preSharedKey))
        .andExpect(status().isForbidden());
    verify(logoutService, never()).logoutUser(any());
  }
}