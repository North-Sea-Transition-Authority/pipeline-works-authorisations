package uk.co.ogauthority.pwa.controller.pwaapplications.shared;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.masterpwa.MasterPwaAuthorisationService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PickExistingPwaController.class)
public class PickExistingPwaControllerTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationService pwaApplicationService;

  @MockBean
  private MasterPwaAuthorisationService masterPwaAuthorisationService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(123),
      Collections.emptyList());

  private MasterPwa masterPwa = new MasterPwa();
  @Before
  public void setup(){

    when(masterPwaAuthorisationService.getMasterPwaIfAuthorised(anyInt(), any())).thenReturn(masterPwa);
    // fake create application service so we get an app of the requested type back
    when(pwaApplicationService.createVariationPwaApplication(any(), any(), any())).thenAnswer( invocation -> {
          PwaApplicationType appType = Arrays.stream(invocation.getArguments())
              .filter(arg -> arg instanceof PwaApplicationType)
              .map(o -> (PwaApplicationType) o)
              .findFirst().orElse(null);
          var application = new PwaApplication();
          application.setApplicationType(appType);
          return application;
        }
    );

  }

  @Test
  public void renderPickPwaToStartApplication_onlySupportedTypesGetOkStatus() throws Exception {
    // TODO PWA-298, PWA-299, PWA-300, PWA-301, PWA-302 as we add support update this test
    var expectOkAppTypes = EnumSet.of(PwaApplicationType.CAT_1_VARIATION);

    for (PwaApplicationType appType : PwaApplicationType.values()) {
      ResultMatcher expectedStatus = expectOkAppTypes.contains(appType) ? status().isOk() : status().isForbidden();
      try {
        mockMvc.perform(
            get(ReverseRouter.route(on(PickExistingPwaController.class)
                .renderPickPwaToStartApplication(appType, null, null)
            )).with(authenticatedUserAndSession(user))
                .with(csrf()))
            .andExpect(expectedStatus);
      } catch (AssertionError e) {
        throw new AssertionError("Failed! appType:" + appType + " Message:" + e.getMessage(), e);
      }
    }
  }

  @Test
  public void pickPwaAndStartApplication_onlySupportedTypesGetOkRedicrectedToTaskList() throws Exception {
    // TODO PWA-298, PWA-299, PWA-300, PWA-301, PWA-302 as we add support update this test
    var expectOkAppTypes = EnumSet.of(PwaApplicationType.CAT_1_VARIATION);

    for (PwaApplicationType appType : PwaApplicationType.values()) {
      ResultMatcher expectedStatus = expectOkAppTypes.contains(appType) ? status().is3xxRedirection() : status().isForbidden();
      try {
        mockMvc.perform(post(ReverseRouter.route(on(PickExistingPwaController.class)
            .pickPwaAndStartApplication(appType, null, null, null))
        )
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("masterPwaId", "1")
        )
            .andExpect(expectedStatus);
      } catch (AssertionError e) {
        throw new AssertionError("Failed! appType:" + appType + " Message:" + e.getMessage(), e);
      }
    }

  }

}
