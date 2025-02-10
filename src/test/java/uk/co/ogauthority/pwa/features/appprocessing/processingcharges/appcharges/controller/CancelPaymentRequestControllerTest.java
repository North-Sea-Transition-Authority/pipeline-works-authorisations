package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
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
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReport;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReportTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.CancelAppChargeFormValidator;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.CancelAppPaymentOutcome;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.CancelPaymentRequestAppProcessingService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummary;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummaryTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CancelPaymentRequestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class CancelPaymentRequestControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private static final int APP_ID = 1;
  private static final int APP_DETAIL_ID = 30;
  private static final PwaApplicationType APP_TYPE = PwaApplicationType.OPTIONS_VARIATION;
  private static final String CANCEL_REASON_ATTR = "cancellationReason";

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private CancelPaymentRequestAppProcessingService cancelPaymentRequestProcService;

  @MockBean
  private ApplicationChargeRequestService applicationChargeRequestService;

  @MockBean
  private ApplicationPaymentSummariser applicationPaymentSummariser;

  @MockBean
  private CancelAppChargeFormValidator cancelAppChargeFormValidator;


  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private ApplicationChargeRequestReport applicationChargeRequestReport;
  private ApplicationPaymentDisplaySummary applicationPaymentDisplaySummary;

  @Before
  public void setUp() throws Exception {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE, APP_ID, APP_DETAIL_ID);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);

    applicationChargeRequestReport = ApplicationChargeRequestReportTestUtil.createOpenReport(
        100,
        "Summary",
        List.of(ApplicationChargeRequestReportTestUtil.createApplicationChargeItem("Item 1", 100))
    );
    applicationPaymentDisplaySummary = ApplicationPaymentDisplaySummaryTestUtil.getDefaultPaymentDisplaySummary();

    when(applicationChargeRequestService.getOpenRequestAsApplicationChargeRequestReport(any()))
        .thenReturn(Optional.of(applicationChargeRequestReport));
    when(applicationPaymentSummariser.summarise(any()))
        .thenReturn(applicationPaymentDisplaySummary);
    when(applicationChargeRequestService.applicationHasOpenChargeRequest(any())).thenReturn(true);
    when(cancelPaymentRequestProcService.taskAccessible(any())).thenReturn(true);
    when(applicationChargeRequestService.cancelPaymentRequest(any(), any(), any())).thenReturn(CancelAppPaymentOutcome.CANCELLED);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(
        ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplicationDetail.getPwaApplication()),
        EnumSet.allOf(PwaAppProcessingPermission.class));

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(permissionsDto);

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService,
        pwaAppProcessingPermissionService)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.CANCEL_PAYMENT);

  }

  @Test
  public void renderCancelPaymentRequest_verifyModel() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(CancelPaymentRequestController.class)
        .renderCancelPaymentRequest(APP_ID, APP_TYPE, null, null)))
        .with(user(user))
    )
        .andExpect(status().isOk())
        .andExpect(model().hasNoErrors())
        .andExpect(model().attributeExists("caseSummaryView"))
        .andExpect(model().attributeExists("cancelUrl"))
        .andExpect(model().attributeExists("appRef"))
        .andExpect(model().attributeExists("appPaymentDisplaySummary"))
        .andExpect(model().attributeExists("pageRef"))
        .andExpect(model().attributeExists("errorList"));

  }

  @Test
  public void renderCancelPaymentRequest_whenCancelTaskNotAccessible() throws Exception {
    when(cancelPaymentRequestProcService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(CancelPaymentRequestController.class)
        .renderCancelPaymentRequest(APP_ID, APP_TYPE, null, null)))
        .with(user(user))
    )
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderCancelPaymentRequest_whenAppChargeReportNotFound() throws Exception {
    when(applicationChargeRequestService.getOpenRequestAsApplicationChargeRequestReport(any())).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(CancelPaymentRequestController.class)
        .renderCancelPaymentRequest(APP_ID, APP_TYPE, null, null)))
        .with(user(user))
    )
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void renderCancelPaymentRequest_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CancelPaymentRequestController.class)
                .renderCancelPaymentRequest(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void cancelPaymentRequest_whenCancelTaskNotAccessible() throws Exception {
    when(cancelPaymentRequestProcService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(CancelPaymentRequestController.class)
        .cancelPaymentRequest(APP_ID, APP_TYPE, null, null, null, null)))
        .with(user(user))
        .with(csrf())
    )
        .andExpect(status().isForbidden());
  }

  @Test
  public void cancelPaymentRequest_whenAppChargeReportNotFound() throws Exception {
    when(applicationChargeRequestService.getOpenRequestAsApplicationChargeRequestReport(any())).thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(CancelPaymentRequestController.class)
        .cancelPaymentRequest(APP_ID, APP_TYPE, null, null, null, null)))
        .with(user(user))
        .with(csrf())
    )
        .andExpect(status().isInternalServerError());
  }


  @Test
  public void cancelPaymentRequest_whenFormInvalid() throws Exception {

    ControllerTestUtils.mockValidatorErrors(cancelAppChargeFormValidator, List.of(CANCEL_REASON_ATTR));

    var result = mockMvc.perform(post(ReverseRouter.route(on(CancelPaymentRequestController.class)
        .cancelPaymentRequest(APP_ID, APP_TYPE, null, null, null, null)))
        .with(user(user))
        .with(csrf())
        .param(CANCEL_REASON_ATTR, "")
    )
        .andExpect(status().isOk());

    verify(applicationChargeRequestService, times(0))
        .cancelPaymentRequest(any(), any(), any());
  }


  @Test
  public void cancelPaymentRequest_whenFormValid() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(CancelPaymentRequestController.class)
        .cancelPaymentRequest(APP_ID, APP_TYPE, null, null, null, null)))
        .with(user(user))
        .with(csrf())
        .param(CANCEL_REASON_ATTR, ValidatorTestUtils.exactlyMaxDefaultCharLength())
    )
        .andExpect(status().is3xxRedirection());

    verify(applicationChargeRequestService, times(1))
        .cancelPaymentRequest(pwaApplicationDetail.getPwaApplication(), user, ValidatorTestUtils.exactlyMaxDefaultCharLength());
  }

  @Test
  public void cancelPaymentRequest_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CancelPaymentRequestController.class)
                .cancelPaymentRequest(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }
}