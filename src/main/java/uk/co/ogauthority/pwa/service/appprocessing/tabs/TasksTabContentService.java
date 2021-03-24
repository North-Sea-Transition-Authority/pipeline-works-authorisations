package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.appprocessing.processingcharges.IndustryPaymentController;
import uk.co.ogauthority.pwa.controller.masterpwas.contacts.PwaContactController;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView;
import uk.co.ogauthority.pwa.model.view.banner.PageBannerView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
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

    Optional<String> payForAppUrl = Optional.empty();
    Optional<String> manageAppContactsUrl = Optional.empty();

    boolean industryFlag = appProcessingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    // only retrieve tasks if we're on the tasks tab to reduce load time
    if (currentTab == AppProcessingTab.TASKS) {

      taskListGroups = appProcessingTaskListService.getTaskListGroups(appProcessingContext);

      updateRequestViewOpt = applicationUpdateRequestViewService.getOpenRequestView(appProcessingContext.getApplicationDetail());

      optionsApprovalPageBannerViewOpt = approveOptionsService.getOptionsApprovalPageBannerView(
          appProcessingContext.getApplicationDetail()
      );

      if (appProcessingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.UPDATE_PUBLIC_NOTICE_DOC)) {
        publicNoticePageBannerViewOpt = publicNoticeDocumentUpdateService.getPublicNoticeUpdatePageBannerView(
            appProcessingContext.getPwaApplication()
        );
      }

      taskListUrl = pwaApplicationRedirectService.getTaskListRoute(appProcessingContext.getPwaApplication());

      if (appProcessingContext.hasProcessingPermission(PwaAppProcessingPermission.PAY_FOR_APPLICATION)) {
        payForAppUrl = Optional.of(ReverseRouter.route(on(IndustryPaymentController.class).renderPayForApplicationLanding(
            appProcessingContext.getMasterPwaApplicationId(), appProcessingContext.getApplicationType(), null
        )));
      }

      if (appProcessingContext.hasProcessingPermission(PwaAppProcessingPermission.MANAGE_APPLICATION_CONTACTS)) {
        manageAppContactsUrl = Optional.of(ReverseRouter.route(on(PwaContactController.class).renderContactsScreen(
           appProcessingContext.getApplicationType(),  appProcessingContext.getMasterPwaApplicationId(), null, null
        )));
      }

    }

    var modelMap = new HashMap<>(Map.of(
        "taskListGroups", taskListGroups,
        "industryFlag", industryFlag,
        "taskListUrl", taskListUrl
    ));

    updateRequestViewOpt.ifPresent(view -> modelMap.put("updateRequestView", view));
    optionsApprovalPageBannerViewOpt.ifPresent(view -> modelMap.put("optionsApprovalPageBanner", view));
    publicNoticePageBannerViewOpt.ifPresent(view -> modelMap.put("publicNoticePageBannerView", view));
    payForAppUrl.ifPresent(s -> modelMap.put("payForAppUrl", s));
    manageAppContactsUrl.ifPresent(s -> modelMap.put("manageAppContactsUrl", s));

    return modelMap;

  }

}
