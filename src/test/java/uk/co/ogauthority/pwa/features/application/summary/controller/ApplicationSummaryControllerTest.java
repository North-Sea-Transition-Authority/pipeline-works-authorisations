package uk.co.ogauthority.pwa.features.application.summary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSummaryViewService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView;
import uk.co.ogauthority.pwa.model.view.appsummary.VisibleApplicationVersionOptionsForUser;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;

@WebMvcTest(controllers = ApplicationSummaryController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
class ApplicationSummaryControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private ApplicationSummaryViewService applicationSummaryViewService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  @BeforeEach
  void setUp() {

    when(applicationSummaryViewService.getVisibleApplicationVersionOptionsForUser(any(), any()))
        .thenReturn(new VisibleApplicationVersionOptionsForUser(Map.of()));
    when(applicationSummaryViewService.getApplicationSummaryViewForAppDetailId(any())).thenReturn(new ApplicationSummaryView("<html>", List.of()));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  void renderSummary_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ApplicationSummaryController.class)
                .renderSummary(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderSummary_applicationDetailNonAvailable_throwsApplicationDetailNotFoundException() {

    when(applicationSummaryViewService.getVisibleApplicationVersionOptionsForUser(any(), any()))
        .thenReturn(new VisibleApplicationVersionOptionsForUser(Map.of("66", "Version 66")));

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ApplicationSummaryController.class)
                .renderSummary(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, 65)));

    endpointTester.performProcessingPermissionCheck(status().isNotFound(), status().isForbidden());

  }

  @Test
  void postViewSummary_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ApplicationSummaryController.class)
                .postViewSummary(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

}
