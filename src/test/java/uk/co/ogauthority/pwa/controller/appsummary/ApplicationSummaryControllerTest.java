package uk.co.ogauthority.pwa.controller.appsummary;

import static org.mockito.ArgumentMatchers.any;
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
import uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView;
import uk.co.ogauthority.pwa.model.view.appsummary.VisibleApplicationVersionOptionsForUser;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSummaryViewService;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ApplicationSummaryController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class ApplicationSummaryControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private ApplicationSummaryViewService applicationSummaryViewService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  @Before
  public void setUp() {

    when(applicationSummaryViewService.getVisibleApplicationVersionOptionsForUser(any(), any()))
        .thenReturn(new VisibleApplicationVersionOptionsForUser(Map.of()));
    when(applicationSummaryViewService.getApplicationSummaryViewForAppDetailId(any())).thenReturn(new ApplicationSummaryView("<html>", List.of()));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  public void renderSummary_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ApplicationSummaryController.class)
                .renderSummary(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderSummary_applicationDetailNonAvailable_throwsApplicationDetailNotFoundException() {

    when(applicationSummaryViewService.getVisibleApplicationVersionOptionsForUser(any(), any()))
        .thenReturn(new VisibleApplicationVersionOptionsForUser(Map.of("66", "Version 66")));

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ApplicationSummaryController.class)
                .renderSummary(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, 65)));

    endpointTester.performProcessingPermissionCheck(status().isNotFound(), status().isForbidden());

  }

  @Test
  public void postViewSummary_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ApplicationSummaryController.class)
                .postViewSummary(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

}
