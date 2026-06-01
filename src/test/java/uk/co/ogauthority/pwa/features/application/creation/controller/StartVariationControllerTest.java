package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.ResolverAbstractControllerTest;
import uk.co.ogauthority.pwa.controller.WithDefaultPageControllerAdvice;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@WebMvcTest(controllers = StartVariationController.class)
@ContextConfiguration(classes = StartVariationController.class)
@WithDefaultPageControllerAdvice
class StartVariationControllerTest extends ResolverAbstractControllerTest {

  private final AuthenticatedUserAccount permittedUser = new AuthenticatedUserAccount(new WebUserAccount(123),
      Set.of(PwaUserPrivilege.PWA_ACCESS));

  private final AuthenticatedUserAccount prohibitedUser = new AuthenticatedUserAccount(new WebUserAccount(999),
      Set.of(PwaUserPrivilege.PWA_ACCESS));

  @BeforeEach
  void setUp() {
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(permittedUser, Map.of(TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR))))
        .thenReturn(true);
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(prohibitedUser, Map.of(TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR))))
        .thenReturn(false);
    doCallRealMethod().when(hasTeamRoleService).userHasAnyRoleInTeamType(any(AuthenticatedUserAccount.class),
        eq(TeamType.ORGANISATION), anySet());
  }

  //todo add PRM back in later - https://fivium.atlassian.net/browse/PRUAT-10
  @ParameterizedTest
  @EnumSource(PwaApplicationType.class)
  void renderVariationTypeStartPage_onlySupportedTypesGetOkStatus_petroleum(PwaApplicationType appType) throws Exception {
    var expectOkAppTypes = EnumSet.of(
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.HUOO_VARIATION,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.DECOMMISSIONING
    );

    mockMvc.perform(
        get(ReverseRouter.route(on(StartVariationController.class).renderVariationTypeStartPage(null, appType, PwaResourceType.PETROLEUM)))
            .with(user(permittedUser))
            .with(csrf()))
        .andExpect(expectOkAppTypes.contains(appType) ? status().isOk() : status().isForbidden());
  }

  //todo add PRM back in later - https://fivium.atlassian.net/browse/PRUAT-10
  @ParameterizedTest
  @EnumSource(PwaApplicationType.class)
  void renderVariationTypeStartPage_onlySupportedTypesGetOkStatus_hydrogen(PwaApplicationType appType) throws Exception {
    var expectOkAppTypes = EnumSet.of(
        PwaApplicationType.CAT_1_VARIATION
    );

    mockMvc.perform(
        get(ReverseRouter.route(on(StartVariationController.class).renderVariationTypeStartPage(null, appType, PwaResourceType.HYDROGEN)))
            .with(user(permittedUser))
            .with(csrf()))
        .andExpect(expectOkAppTypes.contains(appType) ? status().isOk() : status().isForbidden());
  }

  @ParameterizedTest
  @EnumSource(PwaApplicationType.class)
  void renderVariationTypeStartPage_noPrivileges(PwaApplicationType appType) throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(StartVariationController.class).renderVariationTypeStartPage(null, appType, PwaResourceType.PETROLEUM)))
            .with(user(prohibitedUser))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  //todo add PRM back in later - https://fivium.atlassian.net/browse/PRUAT-10
  @ParameterizedTest
  @EnumSource(PwaApplicationType.class)
  void startVariation_onlySupportedTypesGetRedirectedStatus_petroleum(PwaApplicationType appType) throws Exception {
    var expectOkAppTypes = EnumSet.of(
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.HUOO_VARIATION,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.DECOMMISSIONING
    );

    mockMvc.perform(
        post(ReverseRouter.route(on(StartVariationController.class).startVariation(null, appType, PwaResourceType.PETROLEUM)))
            .with(user(permittedUser))
            .with(csrf()))
        .andExpect(expectOkAppTypes.contains(appType) ? status().is3xxRedirection() : status().isForbidden());
  }

  //todo add PRM back in later - https://fivium.atlassian.net/browse/PRUAT-10
  @ParameterizedTest
  @EnumSource(PwaApplicationType.class)
  void startVariation_onlySupportedTypesGetRedirectedStatus_hydrogen(PwaApplicationType appType) throws Exception {
    var expectOkAppTypes = EnumSet.of(
        PwaApplicationType.CAT_1_VARIATION
    );

    mockMvc.perform(
            post(ReverseRouter.route(on(StartVariationController.class).startVariation(null, appType, PwaResourceType.HYDROGEN)))
                .with(user(permittedUser))
                .with(csrf()))
        .andExpect(expectOkAppTypes.contains(appType) ? status().is3xxRedirection() : status().isForbidden());
  }

  @ParameterizedTest
  @EnumSource(PwaApplicationType.class)
  void startVariation_noPrivileges(PwaApplicationType appType) throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(on(StartVariationController.class).startVariation(null, appType, PwaResourceType.HYDROGEN)))
            .with(user(prohibitedUser))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderVariationTypeStartPage_pipelineRecordManagement_returnsForbidden() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(StartVariationController.class)
                .renderVariationTypeStartPage(null, PwaApplicationType.PIPELINE_RECORD_MANAGEMENT, PwaResourceType.PETROLEUM)))
                .with(user(permittedUser))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void startVariation_pipelineRecordManagement_returnsForbidden() throws Exception {
    mockMvc.perform(
            post(ReverseRouter.route(on(StartVariationController.class)
                .startVariation(null, PwaApplicationType.PIPELINE_RECORD_MANAGEMENT, PwaResourceType.PETROLEUM)))
                .with(user(permittedUser))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }
}
