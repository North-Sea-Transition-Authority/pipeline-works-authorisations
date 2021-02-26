package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView;
import uk.co.ogauthority.pwa.model.view.banner.PageBannerView;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeDocumentUpdateService;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.PwaAppProcessingTaskListService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;

@Service
public class TasksTabContentService implements AppProcessingTabContentService {

  private final PwaAppProcessingTaskListService appProcessingTaskListService;
  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final ApproveOptionsService approveOptionsService;
  private final PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService;

  @Autowired
  public TasksTabContentService(PwaAppProcessingTaskListService appProcessingTaskListService,
                                ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
                                PwaApplicationRedirectService pwaApplicationRedirectService,
                                ApproveOptionsService approveOptionsService,
                                PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService) {
    this.appProcessingTaskListService = appProcessingTaskListService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.approveOptionsService = approveOptionsService;
    this.publicNoticeDocumentUpdateService = publicNoticeDocumentUpdateService;
  }

  @Override
  public Map<String, Object> getTabContent(PwaAppProcessingContext appProcessingContext, AppProcessingTab currentTab) {

    List<TaskListGroup> taskListGroups = List.of();
    Optional<ApplicationUpdateRequestView> updateRequestViewOpt = Optional.empty();
    Optional<PageBannerView> optionsApprovalPageBannerViewOpt = Optional.empty();
    Optional<PageBannerView> publicNoticePageBannerViewOpt = Optional.empty();
    String taskListUrl = "";

    boolean industryFlag = appProcessingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    // only retrieve tasks if we're on the tasks tab to reduce load time
    if (currentTab == AppProcessingTab.TASKS) {

      taskListGroups = appProcessingTaskListService.getTaskListGroups(appProcessingContext);

      updateRequestViewOpt = applicationUpdateRequestViewService.getOpenRequestView(appProcessingContext.getApplicationDetail());

      optionsApprovalPageBannerViewOpt = approveOptionsService.getOptionsApprovalPageBannerView(
          appProcessingContext.getApplicationDetail()
      );

      publicNoticePageBannerViewOpt = publicNoticeDocumentUpdateService.getPublicNoticeUpdatePageBannerView(
          appProcessingContext.getPwaApplication()
      );

      taskListUrl = pwaApplicationRedirectService.getTaskListRoute(appProcessingContext.getPwaApplication());

    }

    var modelMap = new HashMap<>(Map.of(
        "taskListGroups", taskListGroups,
        "industryFlag", industryFlag,
        "taskListUrl", taskListUrl
    ));

    updateRequestViewOpt.ifPresent(view -> modelMap.put("updateRequestView", view));
    optionsApprovalPageBannerViewOpt.ifPresent(view -> modelMap.put("optionsApprovalPageBanner", view));
    publicNoticePageBannerViewOpt.ifPresent(view -> modelMap.put("publicNoticePageBannerView", view));

    return modelMap;

  }

}
