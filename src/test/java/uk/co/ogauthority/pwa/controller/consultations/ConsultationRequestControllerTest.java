package uk.co.ogauthority.pwa.controller.consultations;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ConsultationRequestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class, PwaAppProcessingPermissionService.class}))

public class ConsultationRequestControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private ConsultationRequestService consultationRequestService;
  @MockBean
  private ControllerHelperService controllerHelperService;

  private PwaApplicationEndpointTestBuilder endpointTester;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;


  @Before
  public void setUp() {
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, teamService, pwaApplicationDetailService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW);
  }


  @Test
  public void renderRequestConsultation_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationRequestController.class)
                .renderRequestConsultation(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  /* Issues with post test using endpoint tester, to be re-visited.
  @Test
  public void postRequestConsultation() {
    when(consultationRequestService.validate(any(), any(), eq(ValidationType.FULL), any())).thenReturn(new BeanPropertyBindingResult(new ConsultationRequestForm(), "form"));
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationRequestController.class)
                .postRequestConsultation(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }
  */


}
