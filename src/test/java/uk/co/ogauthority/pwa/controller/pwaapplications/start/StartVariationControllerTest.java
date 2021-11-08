package uk.co.ogauthority.pwa.controller.pwaapplications.start;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = StartVariationController.class)
public class StartVariationControllerTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  private PwaAppProcessingContextService pwaAppProcessingContextService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(123),
      Set.of(PwaUserPrivilege.PWA_APPLICATION_CREATE));

  private AuthenticatedUserAccount userNoPrivs = new AuthenticatedUserAccount(new WebUserAccount(999),
      Collections.emptyList());

  @Test
  public void renderVariationTypeStartPage_onlySupportedTypesGetOkStatus() throws Exception {
    var expectOkAppTypes = EnumSet.of(
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.HUOO_VARIATION,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.DECOMMISSIONING
    );

    for (PwaApplicationType appType : PwaApplicationType.values()) {
      ResultMatcher expectedStatus = expectOkAppTypes.contains(appType) ? status().isOk() : status().isForbidden();
      try {
        mockMvc.perform(
            get(ReverseRouter.route(on(StartVariationController.class).renderVariationTypeStartPage(appType)))
                .with(authenticatedUserAndSession(user))
                .with(csrf()))
            .andExpect(expectedStatus);
      } catch (AssertionError e) {
        throw new AssertionError("Failed! appType:" + appType + " Message:" + e.getMessage(), e);
      }
    }

  }

  @Test
  public void renderVariationTypeStartPage_noPrivileges() throws Exception {

    for (PwaApplicationType appType : PwaApplicationType.values()) {

      mockMvc.perform(
          get(ReverseRouter.route(on(StartVariationController.class).renderVariationTypeStartPage(appType)))
              .with(authenticatedUserAndSession(userNoPrivs))
              .with(csrf()))
          .andExpect(status().isForbidden());

    }

  }

  @Test
  public void startVariation_onlySupportedTypesGetRedirectedStatus() throws Exception {
    var expectOkAppTypes = EnumSet.of(
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.HUOO_VARIATION,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.DECOMMISSIONING
    );

    for (PwaApplicationType appType : PwaApplicationType.values()) {
      ResultMatcher expectedStatus = expectOkAppTypes.contains(appType) ? status().is3xxRedirection() : status().isForbidden();
      try {
        mockMvc.perform(
            post(ReverseRouter.route(on(StartVariationController.class).startVariation(appType)))
                .with(authenticatedUserAndSession(user))
                .with(csrf()))
            .andExpect(expectedStatus);
      } catch (AssertionError e) {
        throw new AssertionError("Failed! appType:" + appType + " Message:" + e.getMessage(), e);
      }
    }

  }

  @Test
  public void startVariation_noPrivileges() throws Exception {

    for (PwaApplicationType appType : PwaApplicationType.values()) {

      mockMvc.perform(
          post(ReverseRouter.route(on(StartVariationController.class).startVariation(appType)))
              .with(authenticatedUserAndSession(userNoPrivs))
              .with(csrf()))
          .andExpect(status().isForbidden());

    }

  }

}
