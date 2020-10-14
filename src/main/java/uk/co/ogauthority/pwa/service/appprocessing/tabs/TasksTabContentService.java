package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.PwaAppProcessingTaskListService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;

@Service
public class TasksTabContentService implements AppProcessingTabContentService {

  private final PwaAppProcessingTaskListService appProcessingTaskListService;
  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public TasksTabContentService(PwaAppProcessingTaskListService appProcessingTaskListService,
                                ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
                                PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.appProcessingTaskListService = appProcessingTaskListService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  @Override
  public Map<String, Object> getTabContent(PwaAppProcessingContext appProcessingContext, AppProcessingTab currentTab) {

    List<TaskListGroup> taskListGroups = List.of();
    Optional<ApplicationUpdateRequestView> updateRequestViewOpt = Optional.empty();
    String taskListUrl = "";

    boolean industryFlag = appProcessingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    // only retrieve tasks if we're on the tasks tab to reduce load time
    if (currentTab == AppProcessingTab.TASKS) {

      taskListGroups = appProcessingTaskListService.getTaskListGroups(appProcessingContext);

      updateRequestViewOpt = applicationUpdateRequestViewService.getOpenRequestView(appProcessingContext.getApplicationDetail());

      taskListUrl = pwaApplicationRedirectService.getTaskListRoute(appProcessingContext.getPwaApplication());

    }

    var modelMap = new HashMap<>(Map.of(
        "taskListGroups", taskListGroups,
        "industryFlag", industryFlag,
        "taskListUrl", taskListUrl
    ));

    updateRequestViewOpt.ifPresent(view -> modelMap.put("updateRequestView", view));

    return modelMap;

  }

}
