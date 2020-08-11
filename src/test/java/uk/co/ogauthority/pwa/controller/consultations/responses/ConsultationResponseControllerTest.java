package uk.co.ogauthority.pwa.controller.consultations.responses;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ConsultationResponseController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class, PwaAppProcessingPermissionService.class}))

public class ConsultationResponseControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private ConsultationRequestService consultationRequestService;
  @MockBean
  private ControllerHelperService controllerHelperService;
  @MockBean
  private ConsultationResponseService consultationResponseService;

  private PwaApplicationEndpointTestBuilder endpointTester;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;


  @Before
  public void setUp() {
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, teamService, pwaApplicationDetailService);
  }


  //TODO: PWA-712
  @Test
  public void renderResponder_roleSmokeTest() {
//    endpointTester.setRequestMethod(HttpMethod.GET)
//        .setEndpointUrlProducer((applicationDetail, type) ->
//            ReverseRouter.route(on(AssignResponderController.class)
//                .renderAssignResponder(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, null)));
//
//    endpointTester.performRegulatorRoleCheck(status().isOk(), status().isForbidden());

  }


  @Test
  public void postResponder_roleSmokeTest() {
//    endpointTester.setRequestMethod(HttpMethod.POST)
//        .setEndpointUrlProducer((applicationDetail, type) ->
//            ReverseRouter.route(on(AssignResponderController.class)
//                .postRequestConsultation(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null, null)));
//
//    endpointTester.performRegulatorRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }



}
