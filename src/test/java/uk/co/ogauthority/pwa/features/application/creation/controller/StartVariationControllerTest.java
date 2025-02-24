package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@WebMvcTest(controllers = StartVariationController.class)
@Import(PwaMvcTestConfiguration.class)
class StartVariationControllerTest extends AbstractControllerTest {

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(123),
      Set.of(PwaUserPrivilege.PWA_APPLICATION_CREATE));

  private AuthenticatedUserAccount userNoPrivs = new AuthenticatedUserAccount(new WebUserAccount(999),
      Collections.emptyList());

  @Test
  void renderVariationTypeStartPage_onlySupportedTypesGetOkStatus_petroleum() throws Exception {
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
            get(ReverseRouter.route(on(StartVariationController.class).renderVariationTypeStartPage(appType, PwaResourceType.PETROLEUM)))
                .with(user(user))
                .with(csrf()))
            .andExpect(expectedStatus);
      } catch (AssertionError e) {
        throw new AssertionError("Failed! appType:" + appType + " Message:" + e.getMessage(), e);
      }
    }

  }

  @Test
  void renderVariationTypeStartPage_onlySupportedTypesGetOkStatus_hydrogen() throws Exception {
    var expectOkAppTypes = EnumSet.of(
        PwaApplicationType.CAT_1_VARIATION
    );

    for (PwaApplicationType appType : PwaApplicationType.values()) {
      ResultMatcher expectedStatus = expectOkAppTypes.contains(appType) ? status().isOk() : status().isForbidden();
      try {
        mockMvc.perform(
                get(ReverseRouter.route(on(StartVariationController.class).renderVariationTypeStartPage(appType, PwaResourceType.HYDROGEN)))
                    .with(user(user))
                    .with(csrf()))
            .andExpect(expectedStatus);
      } catch (AssertionError e) {
        throw new AssertionError("Failed! appType:" + appType + " Message:" + e.getMessage(), e);
      }
    }

  }

  @Test
  void renderVariationTypeStartPage_noPrivileges() throws Exception {

    for (PwaApplicationType appType : PwaApplicationType.values()) {

      mockMvc.perform(
          get(ReverseRouter.route(on(StartVariationController.class).renderVariationTypeStartPage(appType, PwaResourceType.PETROLEUM)))
              .with(user(userNoPrivs))
              .with(csrf()))
          .andExpect(status().isForbidden());

    }

  }

  @Test
  void startVariation_onlySupportedTypesGetRedirectedStatus_petroleum() throws Exception {
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
            post(ReverseRouter.route(on(StartVariationController.class).startVariation(appType, PwaResourceType.PETROLEUM)))
                .with(user(user))
                .with(csrf()))
            .andExpect(expectedStatus);
      } catch (AssertionError e) {
        throw new AssertionError("Failed! appType:" + appType + " Message:" + e.getMessage(), e);
      }
    }

  }

  @Test
  void startVariation_onlySupportedTypesGetRedirectedStatus_hydrogen() throws Exception {
    var expectOkAppTypes = EnumSet.of(PwaApplicationType.CAT_1_VARIATION);

    for (PwaApplicationType appType : PwaApplicationType.values()) {
      ResultMatcher expectedStatus = expectOkAppTypes.contains(appType) ? status().is3xxRedirection() : status().isForbidden();
      try {
        mockMvc.perform(
                post(ReverseRouter.route(on(StartVariationController.class).startVariation(appType, PwaResourceType.HYDROGEN)))
                    .with(user(user))
                    .with(csrf()))
            .andExpect(expectedStatus);
      } catch (AssertionError e) {
        throw new AssertionError("Failed! appType:" + appType + " Message:" + e.getMessage(), e);
      }
    }

  }

  @Test
  void startVariation_noPrivileges() throws Exception {

    for (PwaApplicationType appType : PwaApplicationType.values()) {

      mockMvc.perform(
          post(ReverseRouter.route(on(StartVariationController.class).startVariation(appType, PwaResourceType.HYDROGEN)))
              .with(user(userNoPrivs))
              .with(csrf()))
          .andExpect(status().isForbidden());

    }

  }

}
