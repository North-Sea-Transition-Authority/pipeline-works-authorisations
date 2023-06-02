package uk.co.ogauthority.pwa.features.application.tasklist.controllers;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationDisplayUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListEntryFactory;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaViewService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaAppNotificationBannerService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.tasklist.DeleteApplicationController;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;


/**
 * Contains code to construct a generic TaskList ModelAndView for a given application detail and tasks.
 */
@Service
public class TaskListControllerModelAndViewCreator {

  @VisibleForTesting
  static final String TASK_LIST_TEMPLATE_PATH = "pwaApplication/shared/taskList/taskList";

  private final ApplicationBreadcrumbService breadcrumbService;
  private final TaskListEntryFactory taskListEntryFactory;
  private final MasterPwaViewService masterPwaViewService;
  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;
  private final ApproveOptionsService approveOptionsService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PwaAppNotificationBannerService pwaAppNotificationBannerService;

  @Autowired
  public TaskListControllerModelAndViewCreator(ApplicationBreadcrumbService breadcrumbService,
                                               TaskListEntryFactory taskListEntryFactory,
                                               MasterPwaViewService masterPwaViewService,
                                               ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
                                               ApproveOptionsService approveOptionsService,
                                               PwaApplicationDetailService pwaApplicationDetailService,
                                               PwaAppNotificationBannerService pwaAppNotificationBannerService) {
    this.breadcrumbService = breadcrumbService;
    this.taskListEntryFactory = taskListEntryFactory;
    this.masterPwaViewService = masterPwaViewService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.approveOptionsService = approveOptionsService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaAppNotificationBannerService = pwaAppNotificationBannerService;
  }


  public ModelAndView getTaskListModelAndView(PwaApplicationDetail pwaApplicationDetail, List<TaskListGroup> applicationTaskGroups) {

    var modelAndView = new ModelAndView(TASK_LIST_TEMPLATE_PATH)
        .addObject("applicationDisplay",
            PwaApplicationDisplayUtils.getApplicationTypeDisplay(
                pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getResourceType()
            ))
        .addObject("applicationTaskGroups",
            applicationTaskGroups)
        .addObject("submissionTask",
            taskListEntryFactory
                .createReviewAndSubmitTask(pwaApplicationDetail));

    if (pwaApplicationDetail.getPwaApplicationType() != PwaApplicationType.INITIAL) {
      modelAndView.addObject("masterPwaReference",
          masterPwaViewService.getCurrentMasterPwaView(pwaApplicationDetail.getPwaApplication()).getReference());
      modelAndView.addObject("viewPwaUrl",
          ReverseRouter.route(on(PwaViewController.class).renderViewPwa(
              pwaApplicationDetail.getPwaApplication().getMasterPwa().getId(), PwaViewTab.PIPELINES, null, null)));
    }

    pwaAppNotificationBannerService.addParallelPwaApplicationsWarningBannerIfRequired(pwaApplicationDetail.getPwaApplication(),
        modelAndView);

    var canDeleteApplication = pwaApplicationDetailService.applicationDetailCanBeDeleted(pwaApplicationDetail);
    if (canDeleteApplication) {
      modelAndView.addObject("deleteAppUrl",
          ReverseRouter.route(on(DeleteApplicationController.class).renderDeleteApplication(
              pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),null)));
    }
    modelAndView.addObject("canShowDeleteAppButton", canDeleteApplication);

    // if first version, we'll have come from the work area, otherwise can only access via case management screen
    if (pwaApplicationDetail.getVersionNo() == 1) {

      breadcrumbService.fromWorkArea(modelAndView, "Task list");

    } else {

      breadcrumbService.fromCaseManagement(pwaApplicationDetail.getPwaApplication(), modelAndView, "Task list");

      var updateRequestViewOpt = applicationUpdateRequestViewService.getOpenRequestView(pwaApplicationDetail.getPwaApplication());

      var optionsApprovalPageBannerViewOpt = approveOptionsService.getOptionsApprovalPageBannerView(pwaApplicationDetail);

      updateRequestViewOpt.ifPresent(updateRequestView -> modelAndView.addObject("updateRequestView", updateRequestView));
      optionsApprovalPageBannerViewOpt.ifPresent(pageBannerView -> modelAndView.addObject("optionsApprovalPageBanner", pageBannerView));

    }

    return modelAndView;

  }


}
