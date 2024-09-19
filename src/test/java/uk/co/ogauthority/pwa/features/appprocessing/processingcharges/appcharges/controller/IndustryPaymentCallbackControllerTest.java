package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReport;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReportTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ProcessPaymentAttemptOutcome;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.PwaAppChargeRequestTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargePaymentAttempt;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargePaymentAttemptTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequest;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequestDetail;
import uk.co.ogauthority.pwa.features.pwapay.PaymentRequestStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@RunWith(SpringRunner.class)
@WebMvcTest(controllers = IndustryPaymentCallbackController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class IndustryPaymentCallbackControllerTest extends PwaAppProcessingContextAbstractControllerTest {
  private static final int APP_ID = 1;
  private static final int APP_DETAIL_ID = 30;
  private static final PwaApplicationType APP_TYPE = PwaApplicationType.HUOO_VARIATION;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount paymentUser;
  private Person payPerson, pwaManagerPerson;
  private ApplicationChargeRequestReport applicationChargeRequestReport;

  private PwaAppChargeRequestDetail chargeRequestDetail;
  private PwaAppChargeRequest chargeRequest;
  private PwaAppChargePaymentAttempt paymentAttempt;

  private UUID uuid;

  @MockBean
  private ApplicationChargeRequestService applicationChargeRequestService;


  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  public IndustryPaymentCallbackControllerTest() {
  }

  @Before
  public void setUp() throws Exception {
    uuid = UUID.randomUUID();
    payPerson = PersonTestUtil.createPersonFrom(new PersonId(1));
    pwaManagerPerson = PersonTestUtil.createPersonFrom(new PersonId(2));
    paymentUser = new AuthenticatedUserAccount(new WebUserAccount(1, payPerson), EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE, APP_ID, APP_DETAIL_ID);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    chargeRequestDetail = PwaAppChargeRequestTestUtil.createDefaultChargeRequest(
        pwaApplicationDetail.getPwaApplication(), pwaManagerPerson, PwaAppChargeRequestStatus.OPEN);
    chargeRequest = chargeRequestDetail.getPwaAppChargeRequest();

    paymentAttempt = PwaAppChargePaymentAttemptTestUtil.createWithPaymentRequest(chargeRequest,
        PaymentRequestStatus.IN_PROGRESS, payPerson);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(),
        paymentUser))
        .thenReturn(Optional.of(pwaApplicationDetail));

    applicationChargeRequestReport = ApplicationChargeRequestReportTestUtil.createPaidReport(
        100,
        "Summary",
        Instant.now().minus(100, ChronoUnit.SECONDS),
        pwaManagerPerson.getId(),
        Instant.now(),
        payPerson.getId()
    );

    when(applicationChargeRequestService.getLatestRequestAsApplicationChargeRequestReport(any()))
        .thenReturn(Optional.of(applicationChargeRequestReport));

    var permissionsDto = new ProcessingPermissionsDto(null,
        Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, paymentUser))
        .thenReturn(permissionsDto);

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService,
        pwaAppProcessingPermissionService);

  }

  @Test
  public void reconcilePaymentRequestAndRedirect_whenRequestFound_andProcessingHasNotChangedStatusOfChargeRequest() throws Exception {

    when(applicationChargeRequestService.reconcilePaymentRequestCallbackUuidToPaymentAttempt(uuid))
        .thenReturn(paymentAttempt);

    when(applicationChargeRequestService.processPaymentAttempt(paymentAttempt, paymentUser))
        .thenReturn(ProcessPaymentAttemptOutcome.CHARGE_REQUEST_UNCHANGED);

    mockMvc.perform(get(ReverseRouter.route(
        on(IndustryPaymentCallbackController.class).reconcilePaymentRequestAndRedirect(uuid, null, null, Optional.empty())))
        .with(authenticatedUserAndSession(paymentUser)))
        .andExpect(status().is3xxRedirection())
        // cannot use reverse router as app type conversion using url String not done outside of app context
        .andExpect(view().name("redirect:/pwa-application/huoo/1/case-management/TASKS"))
        .andExpect(flash().attributeCount(2));

    verify(analyticsService).sendAnalyticsEvent(any(), eq(AnalyticsEventCategory.PAYMENT_ATTEMPT_NOT_COMPLETED));

  }

  @Test
  public void reconcilePaymentRequestAndRedirect_whenRequestFound_andProcessingHasChangedStatusOfChargeRequest() throws Exception {

    when(applicationChargeRequestService.reconcilePaymentRequestCallbackUuidToPaymentAttempt(uuid))
        .thenReturn(paymentAttempt);

    when(applicationChargeRequestService.processPaymentAttempt(paymentAttempt, paymentUser))
        .thenReturn(ProcessPaymentAttemptOutcome.CHARGE_REQUEST_PAID);

    mockMvc.perform(get(ReverseRouter.route(
        on(IndustryPaymentCallbackController.class).reconcilePaymentRequestAndRedirect(uuid, null, null, Optional.empty())))
        .with(authenticatedUserAndSession(paymentUser)))
        .andExpect(status().is3xxRedirection())
        // cannot use reverse router as app type conversion using url String not done outside of app context
        .andExpect(view().name("redirect:/pwa-application/huoo/1/payment-result"))
        .andExpect(flash().attributeCount(0));

    verify(analyticsService).sendAnalyticsEvent(any(), eq(AnalyticsEventCategory.PAYMENT_ATTEMPT_COMPLETED));

  }

  @Test
  public void reconcilePaymentRequestAndRedirect_whenRequestNotFound() throws Exception {

    when(applicationChargeRequestService.reconcilePaymentRequestCallbackUuidToPaymentAttempt(uuid))
        .thenThrow(new PwaEntityNotFoundException("some error"));

    mockMvc.perform(get(ReverseRouter.route(
        on(IndustryPaymentCallbackController.class).reconcilePaymentRequestAndRedirect(uuid, null, null, Optional.empty())))
        .with(authenticatedUserAndSession(paymentUser)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void renderPaymentResult_whenPaidChargeRequestFound() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(
        on(IndustryPaymentCallbackController.class).renderPaymentResult(APP_ID, APP_TYPE, null)))
        .with(authenticatedUserAndSession(paymentUser)))
        .andExpect(status().isOk())
        .andExpect(model().hasNoErrors())
        .andExpect(model().attributeExists("workAreaUrl"))
        .andExpect(model().attributeExists("caseManagementUrl"))
        .andExpect(model().attributeExists("appRef"));

  }

  @Test
  public void renderPaymentResult_whenPaidChargeNotRequestFound() throws Exception {

    applicationChargeRequestReport = ApplicationChargeRequestReportTestUtil.createCancelledReport(
        100,
        "Summary",
        Instant.now().minus(100, ChronoUnit.SECONDS),
        pwaManagerPerson.getId(),
        Instant.now(),
        pwaManagerPerson.getId()
    );

    when(applicationChargeRequestService.getLatestRequestAsApplicationChargeRequestReport(any()))
        .thenReturn(Optional.of(applicationChargeRequestReport));

    var permissionsDto = new ProcessingPermissionsDto(null,
        Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, paymentUser))
        .thenReturn(permissionsDto);

    mockMvc.perform(get(ReverseRouter.route(
        on(IndustryPaymentCallbackController.class).renderPaymentResult(APP_ID, APP_TYPE, null)))
        .with(authenticatedUserAndSession(paymentUser)))
        .andExpect(status().is5xxServerError());

  }


  @Test
  public void renderPaymentResult_processingPermissionSmokeTest() {
    endpointTester.setAllowedProcessingPermissions(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(IndustryPaymentCallbackController.class)
                .renderPaymentResult(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }
}