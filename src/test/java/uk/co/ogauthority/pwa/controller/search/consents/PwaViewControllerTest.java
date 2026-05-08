package uk.co.ogauthority.pwa.controller.search.consents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaContextAbstractControllerTest;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionService;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewTabService;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.testutils.PwaEndpointTestBuilder;

@WebMvcTest(controllers = PwaViewController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaContextService.class}))
class PwaViewControllerTest extends PwaContextAbstractControllerTest {

  private PwaEndpointTestBuilder endpointTester;

  @MockitoBean
  protected PwaPermissionService pwaPermissionService;

  @MockitoBean
  protected PwaViewTabService pwaViewTabService;

  @MockitoBean
  private TeamQueryService teamQueryService;

  private final Long wuaId = 1L;

  @BeforeEach
  void setUp() {
    endpointTester = new PwaEndpointTestBuilder(mockMvc, masterPwaService, pwaPermissionService, consentSearchService)
        .setAllowedProcessingPermissions(PwaPermission.VIEW_PWA);

    when(pwaViewTabService.getTabContentModelMap(any(), any())).thenReturn(Map.of());
    when(teamQueryService.isUserOnlyPartOfStaticTeam(anyLong(), eq(TeamType.SECONDARY_REGULATOR))).thenReturn(false);
  }

  @Test
  void renderViewPwa_pipelinesTab_processingPermissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(masterPwa ->
            ReverseRouter.route(on(PwaViewController.class)
                .renderViewPwa(masterPwa.getId(), PwaViewTab.PIPELINES, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderViewPwa_consentHistoryTab_processingPermissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(masterPwa ->
            ReverseRouter.route(on(PwaViewController.class)
                .renderViewPwa(masterPwa.getId(), PwaViewTab.CONSENT_HISTORY, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderViewPwa_whenSecondaryRegulatorOnly_andNotPipelineTab_thenForbidden() throws Exception {
    var testUser = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.of(PwaUserPrivilege.PWA_ACCESS));

    when(teamQueryService.isUserOnlyPartOfStaticTeam(wuaId, TeamType.SECONDARY_REGULATOR)).thenReturn(true);
    when(pwaPermissionService.getPwaPermissions(any(), eq(testUser))).thenReturn(Set.of(PwaPermission.VIEW_PWA));

    mockMvc.perform(get(ReverseRouter.route(on(PwaViewController.class)
            .renderViewPwa(1, PwaViewTab.CONSENT_HISTORY, null, null, null)))
        .with(user(testUser)))
        .andExpect(status().isForbidden())
        .andExpect(status().reason("Cannot view the %s tab for pwa %s for user %s"
            .formatted(PwaViewTab.CONSENT_HISTORY.getDisplayName(), 1, testUser.getWuaId())));
  }

  @Test
  void renderViewPwa_whenSecondaryRegulatorOnly_andPipelinesTab_thenOk() throws Exception {
    var testUser = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.of(PwaUserPrivilege.PWA_ACCESS));

    when(teamQueryService.isUserOnlyPartOfStaticTeam(wuaId, TeamType.SECONDARY_REGULATOR)).thenReturn(true);
    when(pwaPermissionService.getPwaPermissions(any(), eq(testUser))).thenReturn(Set.of(PwaPermission.VIEW_PWA));


    mockMvc.perform(get(ReverseRouter.route(on(PwaViewController.class)
            .renderViewPwa(1, PwaViewTab.PIPELINES, null, null, null)))
        .with(user(testUser)))
        .andExpect(status().isOk());
  }

  @Test
  void renderViewPwa_whenSecondaryRegulatorOnly_onlyPipelineTabAvailable() throws Exception {
    var testUser = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.of(PwaUserPrivilege.PWA_ACCESS));


    when(teamQueryService.isUserOnlyPartOfStaticTeam(wuaId, TeamType.SECONDARY_REGULATOR)).thenReturn(true);
    when(pwaPermissionService.getPwaPermissions(any(), eq(testUser))).thenReturn(Set.of(PwaPermission.VIEW_PWA));


    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(PwaViewController.class)
            .renderViewPwa(1, PwaViewTab.PIPELINES, null, null, null)))
        .with(user(testUser)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    var availableTabs = (List<PwaViewTab>) Objects.requireNonNull(modelAndView).getModel().get("availableTabs");
    assertThat(availableTabs).containsExactly(PwaViewTab.PIPELINES);
  }

  @Test
  void renderViewPwa_whenNotSecondaryRegulatorOnly_allTabsAvailable() throws Exception {
    var testUser = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.of(PwaUserPrivilege.PWA_ACCESS));
    when(pwaPermissionService.getPwaPermissions(any(), eq(testUser))).thenReturn(Set.of(PwaPermission.VIEW_PWA));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(PwaViewController.class)
            .renderViewPwa(1, PwaViewTab.PIPELINES, null, null, null)))
        .with(user(testUser)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    var availableTabs = (List<PwaViewTab>) Objects.requireNonNull(modelAndView).getModel().get("availableTabs");
    assertThat(availableTabs).containsExactlyInAnyOrder(PwaViewTab.PIPELINES, PwaViewTab.CONSENT_HISTORY);
  }

  @Test
  void renderViewPwa_showBreadcrumbs_whenParamTrue_thenModelAttributeIsTrue() throws Exception {
    var testUser = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.of(PwaUserPrivilege.PWA_ACCESS));

    when(pwaPermissionService.getPwaPermissions(any(), eq(testUser))).thenReturn(Set.of(PwaPermission.VIEW_PWA));

    mockMvc.perform(get(ReverseRouter.route(on(PwaViewController.class)
            .renderViewPwa(1, PwaViewTab.PIPELINES, null, null, true)))
        .with(user(testUser)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("showBreadcrumbs", true));
  }

  @Test
  void renderViewPwa_showBreadcrumbs_whenParamAbsent_thenModelAttributeIsFalse() throws Exception {
    var testUser = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.of(PwaUserPrivilege.PWA_ACCESS));

    when(pwaPermissionService.getPwaPermissions(any(), eq(testUser))).thenReturn(Set.of(PwaPermission.VIEW_PWA));

    mockMvc.perform(get(ReverseRouter.route(on(PwaViewController.class)
            .renderViewPwa(1, PwaViewTab.PIPELINES, null, null, null)))
        .with(user(testUser)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("showBreadcrumbs", false));
  }

}