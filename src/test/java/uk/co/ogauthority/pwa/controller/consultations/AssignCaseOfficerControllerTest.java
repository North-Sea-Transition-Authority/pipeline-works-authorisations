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
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.consultations.AssignCaseOfficerService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AssignCaseOfficerController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class, PwaAppProcessingPermissionService.class}))

public class AssignCaseOfficerControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private PwaApplicationEndpointTestBuilder endpointTester;

  @MockBean
  private AssignCaseOfficerService assignCaseOfficerService;
  @MockBean
  private WorkflowAssignmentService workflowAssignmentService;
  @MockBean
  private ControllerHelperService controllerHelperService;


  @Before
  public void setUp() {
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, teamService, pwaApplicationDetailService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW);
  }


  @Test
  public void renderAssignCaseOfficer_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AssignCaseOfficerController.class)
                .renderAssignCaseOfficer(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  //TODO: PWA-712 -- post testing issues
  @Test
  public void postRequestConsultation() {
//    when(assignCaseOfficerService.validate(any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new AssignCaseOfficerForm(), "form"));
//
//    endpointTester.setRequestMethod(HttpMethod.POST)
//        .setEndpointUrlProducer((applicationDetail, type) ->
//            ReverseRouter.route(on(AssignCaseOfficerController.class)
//                .postAssignCaseOfficer(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));
//
//    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }



}
