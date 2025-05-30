package uk.co.ogauthority.pwa.features.application.creation.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.controller.ResolverAbstractControllerTest;
import uk.co.ogauthority.pwa.controller.WithDefaultPageControllerAdvice;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.creation.ApplicantOrganisationService;
import uk.co.ogauthority.pwa.features.application.creation.PickPwaForm;
import uk.co.ogauthority.pwa.features.application.creation.PickPwaFormValidator;
import uk.co.ogauthority.pwa.features.application.creation.PickableMasterPwaOptions;
import uk.co.ogauthority.pwa.features.application.creation.PickedPwaRetrievalService;
import uk.co.ogauthority.pwa.features.application.creation.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.features.webapp.SystemAreaAccessService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.TimerMetricTestUtils;

@WebMvcTest(controllers = PickExistingPwaController.class)
@ContextConfiguration(classes = PickExistingPwaController.class)
@WithDefaultPageControllerAdvice
class PickExistingPwaControllerTest extends ResolverAbstractControllerTest {

  private static final int MASTER_PWA_ID = 1;

  @MockBean
  private PickedPwaRetrievalService pickedPwaRetrievalService;

  @MockBean
  private PwaHolderTeamService pwaHolderTeamService;

  @MockBean
  private PwaApplicationCreationService pwaApplicationCreationService;

  @MockBean
  private PickPwaFormValidator pickPwaFormValidator;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;

  @MockBean
  private MetricsProvider metricsProvider;

  @MockBean
  private ApplicantOrganisationService applicantOrganisationService;

  @Mock
  private Appender appender;

  @Captor
  private ArgumentCaptor<LoggingEvent> loggingEventCaptor;


  private Timer timer;

  private final AuthenticatedUserAccount permittedUser = new AuthenticatedUserAccount(new WebUserAccount(123),
      Set.of(PwaUserPrivilege.PWA_ACCESS));

  private final AuthenticatedUserAccount prohibitedUser = new AuthenticatedUserAccount(new WebUserAccount(999),
      Set.of(PwaUserPrivilege.PWA_ACCESS));

  private MasterPwa masterPwa;

  private final PortalOrganisationUnit applicantOrganisation = PortalOrganisationTestUtils.generateOrganisationUnit(1, "ACME");

  @BeforeEach
  void setup() {


    doCallRealMethod().when(pickPwaFormValidator).validate(any(), any(), any(Object[].class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    masterPwa = pwaApplication.getMasterPwa();
    var options = new PickableMasterPwaOptions(Map.of(), Map.of());
    when(pickedPwaRetrievalService.getPickablePwaOptions(any(), any())).thenReturn(options);
    when(pickedPwaRetrievalService.getPickedConsentedPwa(any(), any())).thenReturn(masterPwa);
    when(pickedPwaRetrievalService.getPickedNonConsentedPwa(any(), any())).thenReturn(masterPwa);
    // fake create application service so we get an app of the requested type back
    when(pwaApplicationCreationService.createVariationPwaApplication(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
          PwaApplicationType appType = Arrays.stream(invocation.getArguments())
              .filter(arg -> arg instanceof PwaApplicationType)
              .map(o -> (PwaApplicationType) o)
              .findFirst().orElse(null);
         pwaApplication.setApplicationType(appType);
         return pwaApplicationDetail;
        }
    );

    timer = TimerMetricTestUtils.setupTimerMetric(
        PickExistingPwaController.class, "pwa.startAppTimer", appender);
    when(metricsProvider.getStartAppTimer()).thenReturn(timer);

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(permittedUser, Map.of(TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR))))
        .thenReturn(true);
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(prohibitedUser, Map.of(TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR))))
        .thenReturn(false);
    doCallRealMethod().when(hasTeamRoleService).userHasAnyRoleInTeamType(any(AuthenticatedUserAccount.class),
        eq(TeamType.ORGANISATION), anySet());
  }

  @Test
  void renderPickPwaToStartApplication_onlySupportedTypesGetOkStatus() throws Exception {
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
            get(ReverseRouter.route(on(PickExistingPwaController.class)
                .renderPickPwaToStartApplication(appType, PwaResourceType.PETROLEUM, null, null)
            )).with(user(permittedUser))
                .with(csrf()))
            .andExpect(expectedStatus);
      } catch (AssertionError e) {
        throw new AssertionError("Failed! appType:" + appType + " Message:" + e.getMessage(), e);
      }
    }
  }

  @Test
  void renderPickPwaToStartApplication_noPrivileges() throws Exception {

    for (PwaApplicationType appType : PwaApplicationType.values()) {
      mockMvc.perform(
          get(ReverseRouter.route(on(PickExistingPwaController.class)
              .renderPickPwaToStartApplication(appType,PwaResourceType.PETROLEUM, null, null)))
              .with(user(prohibitedUser))
              .with(csrf()))
          .andExpect(status().isForbidden());
    }

  }

  @Test
  void pickPwaAndStartApplication_urlAppTypeCheck() throws Exception {
    var expectOkAppTypes = EnumSet.of(
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.HUOO_VARIATION,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.DECOMMISSIONING
    );
    for (PwaApplicationType appType : PwaApplicationType.values()) {
      ResultMatcher expectedStatus = expectOkAppTypes.contains(appType)
          ? status().is3xxRedirection() : status().isForbidden();
      try {
        mockMvc.perform(post(ReverseRouter.route(on(PickExistingPwaController.class)
            .pickPwaAndStartApplication(appType, PwaResourceType.PETROLEUM, null, null, null)))
            .with(user(permittedUser))
            .with(csrf())
            .param("consentedMasterPwaId", String.valueOf(MASTER_PWA_ID)))
            .andExpect(expectedStatus);

      } catch (AssertionError e) {
        throw new AssertionError("Failed! appType:" + appType + " Message:" + e.getMessage(), e);
      }
    }

  }

  @Test
  void pickPwaAndStartApplication_consentedPwaPicked() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(PickExistingPwaController.class)
        .pickPwaAndStartApplication(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.HYDROGEN,null, null, null)))
        .with(user(permittedUser))
        .with(csrf())
        .param("consentedMasterPwaId", String.valueOf(MASTER_PWA_ID)))
        .andExpect(status().is3xxRedirection());

    verify(pickedPwaRetrievalService, times(1)).getPickedConsentedPwa(MASTER_PWA_ID, permittedUser);

  }

  @Test
  void pickPwaAndStartApplication_oneApplicantOrg_appCreated() throws Exception {

    when(applicantOrganisationService.getPotentialApplicantOrganisations(any(), any())).thenReturn(Set.of(applicantOrganisation));

    mockMvc.perform(post(ReverseRouter.route(on(PickExistingPwaController.class)
            .pickPwaAndStartApplication(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.PETROLEUM, null, null, null)))
            .with(user(permittedUser))
            .with(csrf())
            .param("consentedMasterPwaId", String.valueOf(MASTER_PWA_ID)))
        .andExpect(status().is3xxRedirection());

    verify(pickedPwaRetrievalService, times(1)).getPickedConsentedPwa(MASTER_PWA_ID, permittedUser);
    verify(pwaApplicationCreationService, times(1)).createVariationPwaApplication(masterPwa, PwaApplicationType.CAT_1_VARIATION, PwaResourceType.PETROLEUM, applicantOrganisation,
        permittedUser);

  }

  @Test
  void pickPwaAndStartApplication_multipleApplicantOrgs_appNotCreated() throws Exception {

    var secondApplicantOrg = PortalOrganisationTestUtils.generateOrganisationUnit(2, "Umbrella");
    when(applicantOrganisationService.getPotentialApplicantOrganisations(any(), any())).thenReturn(Set.of(applicantOrganisation, secondApplicantOrg));

    mockMvc.perform(post(ReverseRouter.route(on(PickExistingPwaController.class)
            .pickPwaAndStartApplication(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.HYDROGEN, null, null, null)))
            .with(user(permittedUser))
            .with(csrf())
            .param("consentedMasterPwaId", String.valueOf(MASTER_PWA_ID)))
        .andExpect(status().is3xxRedirection());

    verify(pickedPwaRetrievalService, times(1)).getPickedConsentedPwa(MASTER_PWA_ID, permittedUser);
    verifyNoInteractions(pwaApplicationCreationService);

  }

  @Test
  void pickPwaAndStartApplication_nonconsentedPwaPicked() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(PickExistingPwaController.class)
        .pickPwaAndStartApplication(PwaApplicationType.DEPOSIT_CONSENT, PwaResourceType.PETROLEUM, null, null, null)))
        .with(user(permittedUser))
        .with(csrf())
        .param("nonConsentedMasterPwaId", String.valueOf(MASTER_PWA_ID)))
        .andExpect(status().is3xxRedirection());

    verify(pickedPwaRetrievalService, times(1)).getPickedNonConsentedPwa(MASTER_PWA_ID, permittedUser);

  }

  @Test
  void pickPwaAndStartApplication_nonconsentedPwaPicked_notDepositType() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(PickExistingPwaController.class)
        .pickPwaAndStartApplication(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.HYDROGEN, null, null, null)))
        .with(user(permittedUser))
        .with(csrf())
        .param("nonConsentedMasterPwaId", String.valueOf(MASTER_PWA_ID)))
        .andExpect(status().is5xxServerError());

  }


  @Test
  void pickPwaAndStartApplication_timerMetricStarted_timeRecordedAndLogged() {

    var controller = new PickExistingPwaController(
        pwaApplicationRedirectService,
        pickedPwaRetrievalService,
        mock(ControllerHelperService.class),
        pwaHolderTeamService,
        pwaApplicationCreationService,
        pickPwaFormValidator,
        metricsProvider,
        applicantOrganisationService,
        mock(SystemAreaAccessService.class));

    var form = new PickPwaForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    controller.pickPwaAndStartApplication(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.PETROLEUM, form,  bindingResult,
        permittedUser);

    TimerMetricTestUtils.assertTimeLogged(loggingEventCaptor, appender, "Variation application started");
  }




}
