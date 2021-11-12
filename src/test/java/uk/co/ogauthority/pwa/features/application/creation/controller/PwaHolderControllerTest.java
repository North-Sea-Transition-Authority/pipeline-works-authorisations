package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermissionService;
import uk.co.ogauthority.pwa.features.application.creation.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;
import uk.co.ogauthority.pwa.testutils.TimerMetricTestUtils;
import uk.co.ogauthority.pwa.validators.PwaHolderFormValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PwaHolderController.class)
public class PwaHolderControllerTest extends AbstractControllerTest {

  private static final int APP_ID = 1;

  @MockBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  private PwaAppProcessingContextService appProcessingContextService;

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

  @Mock
  private Appender appender;

  @Captor
  private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

  private Timer timer;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(123),
      Set.of(PwaUserPrivilege.PWA_APPLICATION_CREATE));

  private AuthenticatedUserAccount userNoPrivs = new AuthenticatedUserAccount(new WebUserAccount(999),
      Collections.emptyList());

  private PortalOrganisationUnit orgUnit;

  private PwaApplicationDetail detail;

  @Before
  public void before() {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.getPwaApplication().setId(APP_ID);

    when(pwaApplicationDetailService.getTipDetail(APP_ID)).thenReturn(detail);
    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    orgUnit = TeamTestingUtils.createOrgUnit();
    when(portalOrganisationsAccessor.getOrganisationUnitById(111)).thenReturn(Optional.of(orgUnit));
    when(portalOrganisationsAccessor.getActiveOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(List.of(orgUnit));

    timer = TimerMetricTestUtils.setupTimerMetric(
        PwaHolderController.class, "pwa.startAppTimer", appender);
    when(metricsProvider.getStartAppTimer()).thenReturn(timer);
  }

  @Test
  public void renderHolderScreen_withAuthenticatedUser() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(PwaHolderController.class)
        .renderHolderScreen(null, null)))
        .with(authenticatedUserAndSession(user))
    ).andExpect(status().isOk());

  }

  @Test
  public void renderHolderScreen_noPrivileges() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(PwaHolderController.class)
        .renderHolderScreen(null, null)))
        .with(authenticatedUserAndSession(userNoPrivs))
    ).andExpect(status().isForbidden());

  }

  @Test
  public void postHolderScreen_withHolderOrgId() throws Exception {

    when(pwaApplicationCreationService.createInitialPwaApplication(orgUnit, user)).thenReturn(detail);
    when(pwaApplicationDetailService.getTipDetail(detail.getPwaApplication().getId())).thenReturn(detail);

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(null, null, user)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("holderOuId", "111"))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void postHolderScreen_withHolderOrgId_noPrivileges() throws Exception {

    when(pwaApplicationCreationService.createInitialPwaApplication(orgUnit, user)).thenReturn(detail);
    when(pwaApplicationDetailService.getTipDetail(detail.getPwaApplication().getId())).thenReturn(detail);

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(null, null, userNoPrivs)))
        .with(authenticatedUserAndSession(userNoPrivs))
        .with(csrf())
        .param("holderOuId", "111"))
        .andExpect(status().isForbidden());

  }

  @Test
  public void postHolderScreen_noHolderOrgSelected() throws Exception {

    when(pwaApplicationCreationService.createInitialPwaApplication(orgUnit, user)).thenReturn(detail);
    when(pwaApplicationDetailService.getTipDetail(detail.getPwaApplication().getId())).thenReturn(detail);

    ControllerTestUtils.mockValidatorErrors(pwaHolderFormValidator, List.of("holderOuId"));

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("holderOuId", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/form/holder"))
        .andExpect(model().attributeHasErrors("form"));

  }


  @Test
  public void postHolderScreen_holderOrgExists_andUserDoesntHaveAccessToOrg() throws Exception {

    when(pwaApplicationCreationService.createInitialPwaApplication(orgUnit, user)).thenReturn(detail);
    when(pwaApplicationDetailService.getTipDetail(detail.getPwaApplication().getId())).thenReturn(detail);

    when(portalOrganisationsAccessor.getOrganisationUnitById(44)).thenReturn(Optional.of(orgUnit));
    when(portalOrganisationsAccessor.getActiveOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("holderOuId", "44"))
        .andExpect(status().is4xxClientError());

  }


  @Test
  public void postHolderScreen_timerMetricStarted_timeRecordedAndLogged() {

    when(pwaApplicationCreationService.createInitialPwaApplication(orgUnit, user)).thenReturn(detail);
    when(pwaApplicationDetailService.getTipDetail(detail.getPwaApplication().getId())).thenReturn(detail);
    when(portalOrganisationsAccessor.getOrganisationUnitById(anyInt())).thenReturn(Optional.of(orgUnit));

    var controller = new PwaHolderController(teamService, pwaApplicationCreationService, pwaApplicationDetailService,
        portalOrganisationsAccessor, pwaApplicationRedirectService, pwaHolderFormValidator, padOrganisationRoleService,
        Mockito.mock(ControllerHelperService.class), "", metricsProvider);

    var form = new PwaHolderForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    controller.postHolderScreen(form, bindingResult, user);

    TimerMetricTestUtils.assertTimeLogged(loggingEventCaptor, appender, "Initial application started");
  }

}
