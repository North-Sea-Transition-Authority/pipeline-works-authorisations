package uk.co.ogauthority.pwa.service.pwaapplications.generic.tasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaViewService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListEntryFactory;


/**
 * Contains code to constructr a generic TaskList ModelAndView for a given application detail and tasks.
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

  @Autowired
  public TaskListControllerModelAndViewCreator(ApplicationBreadcrumbService breadcrumbService,
                         TaskListEntryFactory taskListEntryFactory,
                         MasterPwaViewService masterPwaViewService,
                         ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
                         ApproveOptionsService approveOptionsService) {
    this.breadcrumbService = breadcrumbService;
    this.taskListEntryFactory = taskListEntryFactory;
    this.masterPwaViewService = masterPwaViewService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.approveOptionsService = approveOptionsService;
  }


  public ModelAndView getTaskListModelAndView(PwaApplicationDetail pwaApplicationDetail, List<TaskListGroup> applicationTaskGroups) {

    var modelAndView = new ModelAndView(TASK_LIST_TEMPLATE_PATH)
        .addObject("applicationType", pwaApplicationDetail.getPwaApplicationType().getDisplayName())
        .addObject("applicationTaskGroups", applicationTaskGroups)
        .addObject("submissionTask", taskListEntryFactory.createReviewAndSubmitTask(pwaApplicationDetail));

    if (pwaApplicationDetail.getPwaApplicationType() != PwaApplicationType.INITIAL) {
      modelAndView.addObject("masterPwaReference",
          masterPwaViewService.getCurrentMasterPwaView(pwaApplicationDetail.getPwaApplication()).getReference());
    }

    var canShowDeleteAppButton = false;
    if (pwaApplicationDetail.getStatus() == PwaApplicationStatus.DRAFT && pwaApplicationDetail.isFirstVersion()) {
      canShowDeleteAppButton = true;
      modelAndView.addObject("deleteAppUrl",
          ReverseRouter.route(on(DeleteApplicationController.class).renderDeleteApplication(
              pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),null)));
    }
    modelAndView.addObject("canShowDeleteAppButton", canShowDeleteAppButton);

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
