package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.appprocessing.casehistory.CaseHistoryService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.PwaAppProcessingTaskListService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;

@Service
public class AppProcessingTabService {

  private final PwaAppProcessingTaskListService appProcessingTaskListService;
  private final CaseHistoryService caseHistoryService;

  @Autowired
  public AppProcessingTabService(PwaAppProcessingTaskListService appProcessingTaskListService,
                                 CaseHistoryService caseHistoryService) {
    this.appProcessingTaskListService = appProcessingTaskListService;
    this.caseHistoryService = caseHistoryService;
  }

  public List<AppProcessingTab> getTabsAvailableToUser(AuthenticatedUserAccount webUserAccount) {

    var tabList = new ArrayList<AppProcessingTab>();
    tabList.add(AppProcessingTab.TASKS);

    var userPrivs = webUserAccount.getUserPrivileges();

    if (userPrivs.contains(PwaUserPrivilege.PWA_REGULATOR) || userPrivs.contains(PwaUserPrivilege.PWA_CONSULTEE)) {
      tabList.add(AppProcessingTab.CASE_HISTORY);
    }

    if (userPrivs.contains(PwaUserPrivilege.PWA_INDUSTRY)) {
      tabList.add(AppProcessingTab.FIRS);
    }

    return tabList;

  }

  public Map<String, ?> getTabContentModelMap(PwaAppProcessingContext appProcessingContext,
                                              AppProcessingTab tab) {

    List<TaskListGroup> taskListGroups = List.of();
    List<CaseHistoryItemView> caseHistoryItems = List.of();

    // only retrieve tasks if we're on the tasks tab to reduce load time
    if (tab == AppProcessingTab.TASKS) {
      taskListGroups = appProcessingTaskListService.getTaskListGroups(appProcessingContext);
    }

    // only retrieve case history if we're on that tab to reduce load time
    if (tab == AppProcessingTab.CASE_HISTORY) {
      caseHistoryItems = caseHistoryService.getCaseHistory(appProcessingContext.getPwaApplication());
    }

    boolean industryFlag = appProcessingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    return Map.of(
        "taskListGroups", taskListGroups,
        "caseHistoryItems", caseHistoryItems,
        "industryFlag", industryFlag
    );

  }

}
