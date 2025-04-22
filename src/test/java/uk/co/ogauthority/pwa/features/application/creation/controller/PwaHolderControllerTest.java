package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.controller.ResolverAbstractControllerTest;
import uk.co.ogauthority.pwa.controller.WithDefaultPageControllerAdvice;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermissionService;
import uk.co.ogauthority.pwa.features.application.creation.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.features.webapp.SystemAreaAccessService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationSearchUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;
import uk.co.ogauthority.pwa.testutils.TimerMetricTestUtils;
import uk.co.ogauthority.pwa.validators.PwaHolderFormValidator;

@WebMvcTest(controllers = PwaHolderController.class)
@ContextConfiguration(classes = PwaHolderController.class)
@WithDefaultPageControllerAdvice
class PwaHolderControllerTest extends ResolverAbstractControllerTest {

  private static final int APP_ID = 1;

  @MockBean
  private PwaApplicationCreationService pwaApplicationCreationService;

  @MockBean
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @MockBean
  private PwaHolderFormValidator pwaHolderFormValidator;

  @MockBean
  private PwaApplicationPermissionService pwaApplicationPermissionService;

  @MockBean
  private PadOrganisationRoleService padOrganisationRoleService;

  @MockBean
  private MetricsProvider metricsProvider;

  @MockBean
  private PwaHolderTeamService pwaHolderTeamService;


  @Mock
  private Appender appender;

  @Captor
  private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

  private Timer timer;

  private final AuthenticatedUserAccount permittedUser = new AuthenticatedUserAccount(new WebUserAccount(123),
      Set.of(PwaUserPrivilege.PWA_ACCESS));

  private final AuthenticatedUserAccount prohibitedUser = new AuthenticatedUserAccount(new WebUserAccount(999),
      Set.of(PwaUserPrivilege.PWA_ACCESS));

  private PortalOrganisationUnit orgUnit;

  private PortalOrganisationSearchUnit orgSearchUnit;

  private PwaApplicationDetail detail;

  @BeforeEach
  void before() {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.getPwaApplication().setId(APP_ID);

    when(pwaApplicationDetailService.getTipDetailByAppId(APP_ID)).thenReturn(detail);
    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    orgUnit = TeamTestingUtils.createOrgUnit();
    orgSearchUnit = TeamTestingUtils.createOrgSearchUnit();

    when(portalOrganisationsAccessor.getOrganisationUnitById(111)).thenReturn(Optional.of(orgUnit));
    when(portalOrganisationsAccessor.getOrganisationUnitById(OrganisationUnitId.fromInt(111))).thenReturn(Optional.of(orgUnit));

    when(portalOrganisationsAccessor.getActiveOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(List.of(orgUnit));
    when(portalOrganisationsAccessor.getSearchableOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(List.of(orgSearchUnit));

    timer = TimerMetricTestUtils.setupTimerMetric(
        PwaHolderController.class, "pwa.startAppTimer", appender);
    when(metricsProvider.getStartAppTimer()).thenReturn(timer);

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(permittedUser, Map.of(TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR))))
        .thenReturn(true);
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(prohibitedUser, Map.of(TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR))))
        .thenReturn(false);
    doCallRealMethod().when(hasTeamRoleService).userHasAnyRoleInTeamType(any(AuthenticatedUserAccount.class),
        eq(TeamType.ORGANISATION), anySet());
  }

  @Test
  void renderHolderScreen_withAuthenticatedUser() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(PwaHolderController.class)
        .renderHolderScreen(null, PwaResourceType.PETROLEUM, null)))
        .with(user(permittedUser))
    ).andExpect(status().isOk());

  }

  @Test
  void renderHolderScreen_noPrivileges() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(PwaHolderController.class)
        .renderHolderScreen(null, PwaResourceType.PETROLEUM, null)))
        .with(user(prohibitedUser))
    ).andExpect(status().isForbidden());

  }

  @Test
  void postHolderScreen_withHolderOrgId() throws Exception {

    when(pwaApplicationCreationService.createInitialPwaApplication(orgUnit, permittedUser, PwaResourceType.PETROLEUM)).thenReturn(detail);
    when(pwaApplicationDetailService.getTipDetailByAppId(detail.getPwaApplication().getId())).thenReturn(detail);

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(null, PwaResourceType.PETROLEUM, null, permittedUser)))
        .with(user(permittedUser))
        .with(csrf())
        .param("holderOuId", "111"))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  void postHolderScreen_withHolderOrgId_noPrivileges() throws Exception {

    when(pwaApplicationCreationService.createInitialPwaApplication(orgUnit, permittedUser, PwaResourceType.PETROLEUM)).thenReturn(detail);
    when(pwaApplicationDetailService.getTipDetailByAppId(detail.getPwaApplication().getId())).thenReturn(detail);

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(null, PwaResourceType.PETROLEUM, null, prohibitedUser)))
        .with(user(prohibitedUser))
        .with(csrf())
        .param("holderOuId", "111"))
        .andExpect(status().isForbidden());

  }

  @Test
  void postHolderScreen_noHolderOrgSelected() throws Exception {

    when(pwaApplicationCreationService.createInitialPwaApplication(orgUnit, permittedUser, PwaResourceType.PETROLEUM)).thenReturn(detail);
    when(pwaApplicationDetailService.getTipDetailByAppId(detail.getPwaApplication().getId())).thenReturn(detail);

    ControllerTestUtils.mockValidatorErrors(pwaHolderFormValidator, List.of("holderOuId"));

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(null, PwaResourceType.PETROLEUM, null, null)))
        .with(user(permittedUser))
        .with(csrf())
        .param("holderOuId", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/form/holder"))
        .andExpect(model().attributeHasErrors("form"));

  }


  @Test
  void postHolderScreen_holderOrgExists_andUserDoesntHaveAccessToOrg() throws Exception {

    when(pwaApplicationCreationService.createInitialPwaApplication(orgUnit, permittedUser, PwaResourceType.PETROLEUM)).thenReturn(detail);
    when(pwaApplicationDetailService.getTipDetailByAppId(detail.getPwaApplication().getId())).thenReturn(detail);

    when(portalOrganisationsAccessor.getOrganisationUnitById(44)).thenReturn(Optional.of(orgUnit));
    when(portalOrganisationsAccessor.getActiveOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(null, PwaResourceType.PETROLEUM, null, null)))
        .with(user(permittedUser))
        .with(csrf())
        .param("holderOuId", "44"))
        .andExpect(status().is4xxClientError());

  }


  @Test
  void postHolderScreen_timerMetricStarted_timeRecordedAndLogged() {

    when(pwaApplicationCreationService.createInitialPwaApplication(orgUnit, permittedUser, PwaResourceType.PETROLEUM)).thenReturn(detail);
    when(pwaApplicationDetailService.getTipDetailByAppId(detail.getPwaApplication().getId())).thenReturn(detail);
    when(portalOrganisationsAccessor.getOrganisationUnitById(anyInt())).thenReturn(Optional.of(orgUnit));

    var controller = new PwaHolderController(pwaApplicationCreationService, pwaApplicationDetailService,
        portalOrganisationsAccessor, pwaApplicationRedirectService, pwaHolderFormValidator, padOrganisationRoleService,
        mock(ControllerHelperService.class), "", metricsProvider, mock(PwaHolderTeamService.class), mock(SystemAreaAccessService.class));

    var form = new PwaHolderForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    controller.postHolderScreen(form, PwaResourceType.PETROLEUM, bindingResult, permittedUser);

    TimerMetricTestUtils.assertTimeLogged(loggingEventCaptor, appender, "Initial application started");
  }

}
