package uk.co.ogauthority.pwa.controller.appprocessing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Map;
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
import uk.co.ogauthority.pwa.service.appprocessing.application.ConfirmSatisfactoryApplicationService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTabService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CaseManagementController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class CaseManagementControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private PwaApplicationEndpointTestBuilder endpointTester;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService;

  @MockBean
  private AppProcessingTabService appProcessingTabService;

  @Before
  public void setUp() {

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedProcessingPermissions(
            PwaAppProcessingPermission.CASE_MANAGEMENT_OGA,
            PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY,
            PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE);

  }


  @Test
  public void renderCaseManagement_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CaseManagementController.class)
                .renderCaseManagement(applicationDetail.getMasterPwaApplicationId(), type, null,null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderCaseManagement_permissionSmokeTest() {

    when(appProcessingTabService.getTabsAvailableToUser(any())).thenReturn(List.of(AppProcessingTab.TASKS));

    var map = Map.of(
        "taskListGroups", List.of(),
        "industryFlag", false,
        "taskListUrl", "#"
    );

    doReturn(map).when(appProcessingTabService).getTabContentModelMap(any(), any());

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CaseManagementController.class)
                .renderCaseManagement(applicationDetail.getMasterPwaApplicationId(), type, AppProcessingTab.TASKS,null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }



}
