package uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.ApplicationFeeReport;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.ApplicationFeeService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummary;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummaryTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview.InitialReviewFormValidator;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview.InitialReviewPaymentDecision;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview.InitialReviewService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = InitialReviewController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
class InitialReviewControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private static final String CASE_OFFICER_ID_ATTR = "caseOfficerPersonId";
  private static final String REVIEW_DECISION_ATTR = "initialReviewPaymentDecision";
  private static final String PAYMENT_WAIVED_ATTR = "paymentWaivedReason";

  private static final String HEADLINE_FEE_DESC = "FEE_HEAD";
  private static final String FEE_ITEM_DESC = "FEE_ITEM";
  private static final int FEE_AMOUNT = 100;


  private PwaApplicationEndpointTestBuilder endpointTester;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private int APP_ID = 1;

  @MockBean
  private WorkflowAssignmentService workflowAssignmentService;

  @MockBean
  private InitialReviewService initialReviewService;

  @MockBean
  private InitialReviewFormValidator validator;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @MockBean
  private ApplicationFeeService applicationFeeService;

  @MockBean
  private ApplicationPaymentSummariser applicationPaymentSummariser;

  @Mock
  private ApplicationFeeReport applicationFeeReport;

  private ApplicationPaymentDisplaySummary applicationPaymentDisplaySummary;

  @BeforeEach
  void setUp() {

    applicationPaymentDisplaySummary = ApplicationPaymentDisplaySummaryTestUtil.getDefaultPaymentDisplaySummary();
    when(applicationFeeService.getApplicationFeeReport(any())).thenReturn(applicationFeeReport);
    when(applicationPaymentSummariser.summarise(applicationFeeReport)).thenReturn(applicationPaymentDisplaySummary);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));
  }

  @Test
  void renderInitialReview_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(InitialReviewController.class)
                .renderInitialReview(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderInitialReview_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(InitialReviewController.class)
                .renderInitialReview(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderInitialReview_openUpdateRequest_denied() throws Exception {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(InitialReviewController.class)
        .renderInitialReview(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(user(user)))
        .andExpect(status().isForbidden());

  }

  @Test
  void postInitialReview_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(InitialReviewController.class)
                .postInitialReview(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, null)))
        .addRequestParam(CASE_OFFICER_ID_ATTR, "1");

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postInitialReview_paymentWaived() throws Exception {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(InitialReviewController.class).postInitialReview(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(user(user))
        .param(CASE_OFFICER_ID_ATTR, "5")
        .param(REVIEW_DECISION_ATTR, InitialReviewPaymentDecision.PAYMENT_WAIVED.name())
        .param(PAYMENT_WAIVED_ATTR, "REASON")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(initialReviewService, times(1))
        .acceptApplication(pwaApplicationDetail,
            new PersonId(5),
            InitialReviewPaymentDecision.PAYMENT_WAIVED,
            "REASON",
            user);

  }

  @Test
  void postInitialReview_paymentRequired() throws Exception {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(InitialReviewController.class).postInitialReview(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(user(user))
        .param(CASE_OFFICER_ID_ATTR, "5")
        .param(REVIEW_DECISION_ATTR, InitialReviewPaymentDecision.PAYMENT_REQUIRED.name())
        // still provide this and verify its ignored by service call
        .param(PAYMENT_WAIVED_ATTR, "REASON")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(initialReviewService, times(1))
        .acceptApplication(pwaApplicationDetail,
            new PersonId(5),
            InitialReviewPaymentDecision.PAYMENT_REQUIRED,
            null,
            user);

  }

  @Test
  void postInitialReview_alreadyPerformed() throws Exception {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    doThrow(new ActionAlreadyPerformedException("")).when(initialReviewService).acceptApplication(
        any(), any(), any(), any(), any());

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(InitialReviewController.class).postInitialReview(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(user(user))
        .param("caseOfficerPersonId", "5")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    //TODO PWA-1363: Check flash attributes to assert that the error text has been set
  }

  @Test
  void postInitialReview_validationFail() throws Exception {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("caseOfficerPersonId"));

    mockMvc.perform(post(ReverseRouter.route(on(InitialReviewController.class).postInitialReview(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(user(user))
        .param("caseOfficerPersonId", "5")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/appProcessing/initialReview/initialReview"));

  }

  @Test
  void postInitialReview_openUpdateRequest_denied() throws Exception {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail)).thenReturn(true);

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(InitialReviewController.class).postInitialReview(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(user(user))
        .param("caseOfficerPersonId", "5")
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

}
