package uk.co.ogauthority.pwa.controller.consultations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ConsultationController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class ConsultationControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private PwaApplicationEndpointTestBuilder viewAllConsultationsEndpointTester;
  private PwaApplicationEndpointTestBuilder withdrawConsultationEndpointTester;

  @MockBean
  private ConsultationRequestService consultationRequestService;
  @MockBean
  private ConsultationViewService consultationViewService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @Before
  public void setUp() {

    viewAllConsultationsEndpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS);

    withdrawConsultationEndpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.WITHDRAW_CONSULTATION);

  }


  @Test
  public void renderConsultation_appStatusSmokeTest() {

    viewAllConsultationsEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationController.class)
                .renderConsultation(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    viewAllConsultationsEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderConsultation_processingPermissionSmokeTest() {

    viewAllConsultationsEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationController.class)
                .renderConsultation(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    viewAllConsultationsEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderWithdrawConsultation_appStatusSmokeTest() {

    ConsultationRequestView consultationRequestView = new ConsultationRequestView(
        1, "", Instant.now(), ConsultationRequestStatus.ALLOCATION, "", true, null, null);
    when(consultationViewService.getConsultationRequestView(any())).thenReturn(consultationRequestView);
    when(consultationRequestService.canWithDrawConsultationRequest(any())).thenReturn(true);

    withdrawConsultationEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationController.class)
                .renderWithdrawConsultation(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null)));

    withdrawConsultationEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderWithdrawConsultation_permissionSmokeTest() {

    ConsultationRequestView consultationRequestView = new ConsultationRequestView(
        1, "", Instant.now(), ConsultationRequestStatus.ALLOCATION, "", true, null, null);
    when(consultationViewService.getConsultationRequestView(any())).thenReturn(consultationRequestView);
    when(consultationRequestService.canWithDrawConsultationRequest(any())).thenReturn(true);

    withdrawConsultationEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationController.class)
                .renderWithdrawConsultation(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null)));

    withdrawConsultationEndpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

}
