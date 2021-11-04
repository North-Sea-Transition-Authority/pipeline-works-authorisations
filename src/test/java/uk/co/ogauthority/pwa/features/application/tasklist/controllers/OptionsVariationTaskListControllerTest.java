package uk.co.ogauthority.pwa.features.application.tasklist.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = OptionsVariationTaskListController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class OptionsVariationTaskListControllerTest extends TaskListControllerTest {

  private PwaApplicationDetail detail;

  private PwaApplicationEndpointTestBuilder endpointTester;

  @Before
  public void setUp() {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    var taskListGroupList = List.of(
        new TaskListGroup("group", 1, List.of(
            new TaskListEntry("task", "/route", true, 10)))
    );

    when(taskListService.getTaskListGroups(detail)).thenReturn(taskListGroupList);
    when(taskListControllerModelAndViewCreator.getTaskListModelAndView(detail, taskListGroupList))
        .thenCallRealMethod();

    when(pwaApplicationDetailService.getTipDetail(anyInt())).thenReturn(detail);
    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);
  }


  @Test
  public void viewTaskList_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(OptionsVariationTaskListController.class)
                .viewTaskList(
                    applicationDetail.getMasterPwaApplicationId(),
                    null
                )
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void viewTaskList_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(OptionsVariationTaskListController.class)
                .viewTaskList(
                    applicationDetail.getMasterPwaApplicationId(),
                    null
                )
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void viewTaskList_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(OptionsVariationTaskListController.class)
                .viewTaskList(
                    applicationDetail.getMasterPwaApplicationId(),
                    null
                )
            )
        );

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }
}