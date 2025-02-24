package uk.co.ogauthority.pwa.controller.consultations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.ConsultationService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;
import uk.co.ogauthority.pwa.service.consultations.WithdrawConsultationService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = ConsultationController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
class ConsultationControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private PwaApplicationEndpointTestBuilder viewAllConsultationsEndpointTester;
  private PwaApplicationEndpointTestBuilder withdrawConsultationEndpointTester;

  @MockBean
  private ConsultationViewService consultationViewService;

  @MockBean
  private ConsultationService consultationService;

  @MockBean
  private WithdrawConsultationService withdrawConsultationService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {

    viewAllConsultationsEndpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW, PwaApplicationStatus.COMPLETE)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS);

    withdrawConsultationEndpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.WITHDRAW_CONSULTATION);

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of(PwaUserPrivilege.PWA_ACCESS));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(
        pwaApplicationDetail.getPwaApplication()), EnumSet.allOf(PwaAppProcessingPermission.class));

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);
    when(consultationService.getTaskState(any())).thenReturn(TaskState.EDIT);

  }


  @Test
  void renderConsultation_appStatusSmokeTest() {

    viewAllConsultationsEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationController.class)
                .renderConsultations(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    viewAllConsultationsEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderConsultation_processingPermissionSmokeTest() {

    viewAllConsultationsEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationController.class)
                .renderConsultations(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    viewAllConsultationsEndpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderConsultation_noSatisfactoryVersions() throws Exception {

    when(processingPermissionService.getProcessingPermissionsDto(any(), any())).thenReturn(new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(pwaApplicationDetail.getPwaApplication()),
        EnumSet.allOf(PwaAppProcessingPermission.class)));

    mockMvc.perform(get(ReverseRouter.route(on(ConsultationController.class).renderConsultations(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  void renderConsultations_userCannotWithdrawConsultations_withdrawLinkNotOnPage() throws Exception {
    var permissionsDto = new ProcessingPermissionsDto(PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(
      pwaApplicationDetail.getPwaApplication()), Set.of(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS));

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(get(ReverseRouter.route(on(ConsultationController.class).renderConsultations(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null)))
      .with(user(user))
      .with(csrf()))
      .andExpect(status().isOk())
      .andExpect(model().attribute("userCanWithdrawConsultations", false));
  }

  @Test
  void renderConsultations_userCanWithdrawConsultations_withdrawLinkOnPage() throws Exception {
    var permissionsDto = new ProcessingPermissionsDto(PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(
      pwaApplicationDetail.getPwaApplication()), Set.of(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS, PwaAppProcessingPermission.WITHDRAW_CONSULTATION));

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(get(ReverseRouter.route(on(ConsultationController.class).renderConsultations(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null)))
      .with(user(user))
      .with(csrf()))
      .andExpect(status().isOk())
      .andExpect(model().attribute("userCanWithdrawConsultations", true));
  }

  @Test
  void renderWithdrawConsultation_appStatusSmokeTest() {

    ConsultationRequestView consultationRequestView = new ConsultationRequestView(
        1, "", Instant.now(), ConsultationRequestStatus.ALLOCATION, "", List.of(), true, null, null, ConsultationResponseDocumentType.DEFAULT);
    when(consultationViewService.getConsultationRequestView(any())).thenReturn(consultationRequestView);
    when(withdrawConsultationService.canWithDrawConsultationRequest(any())).thenReturn(true);

    withdrawConsultationEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationController.class)
                .renderWithdrawConsultation(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null)));

    withdrawConsultationEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderWithdrawConsultation_permissionSmokeTest() {

    ConsultationRequestView consultationRequestView = new ConsultationRequestView(
        1, "", Instant.now(), ConsultationRequestStatus.ALLOCATION, "", List.of(), true, null, null, ConsultationResponseDocumentType.DEFAULT);
    when(consultationViewService.getConsultationRequestView(any())).thenReturn(consultationRequestView);
    when(withdrawConsultationService.canWithDrawConsultationRequest(any())).thenReturn(true);

    withdrawConsultationEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationController.class)
                .renderWithdrawConsultation(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null)));

    withdrawConsultationEndpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderWithdrawConsultation_noSatisfactoryVersions() throws Exception {

    var request = new ConsultationRequest();
    request.setId(1);

    when(processingPermissionService.getProcessingPermissionsDto(any(), any())).thenReturn(new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("group", request),
        EnumSet.allOf(PwaAppProcessingPermission.class)));

    mockMvc.perform(get(ReverseRouter.route(on(ConsultationController.class).renderWithdrawConsultation(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), 1, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

}
