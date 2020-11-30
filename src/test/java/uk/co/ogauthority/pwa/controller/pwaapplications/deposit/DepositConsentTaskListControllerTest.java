package uk.co.ogauthority.pwa.controller.pwaapplications.deposit;

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
import uk.co.ogauthority.pwa.controller.TaskListControllerTest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DepositConsentTaskListController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class DepositConsentTaskListControllerTest extends TaskListControllerTest {

  private PwaApplicationDetail detail;

  private PwaApplicationEndpointTestBuilder endpointTester;

  @Before
  public void setUp() {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.DEPOSIT_CONSENT);

    var taskListGroupList = List.of(
        new TaskListGroup("group", 1, List.of(
            new TaskListEntry("task", "/route", true, 10)))
    );

    when(taskListService.getTaskListGroups(detail)).thenReturn(taskListGroupList);
    when(taskListControllerModelAndViewCreator.getTaskListModelAndView(detail, taskListGroupList))
        .thenCallRealMethod();

    when(pwaApplicationDetailService.getTipDetail(anyInt())).thenReturn(detail);
    when(pwaContactService.getContactRoles(any(), any())).thenReturn(EnumSet.allOf(PwaContactRole.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaContactService, pwaApplicationDetailService)
        .setAllowedTypes(PwaApplicationType.DEPOSIT_CONSENT)
        .setAllowedContactRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);
  }


  @Test
  public void viewTaskList_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(DepositConsentTaskListController.class)
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
            ReverseRouter.route(on(DepositConsentTaskListController.class)
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
            ReverseRouter.route(on(DepositConsentTaskListController.class)
                .viewTaskList(
                    applicationDetail.getMasterPwaApplicationId(),
                    null
                )
            )
        );

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }
}