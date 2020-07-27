package uk.co.ogauthority.pwa.service.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.initialreview.InitialReviewController;
import uk.co.ogauthority.pwa.controller.consultations.CaseManagementController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.AssignedTaskInstance;

@Service
public class WorkAreaService {

  public static final int PAGE_SIZE = 10;

  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final PwaAppProcessingPermissionService appProcessingPermissionService;
  private final ApplicationDetailSearcher applicationDetailSearcher;
  private final PwaContactService pwaContactService;

  @Autowired
  public WorkAreaService(PwaContactService pwaContactService,
                         ApplicationDetailSearcher applicationDetailSearcher,
                         PwaApplicationRedirectService pwaApplicationRedirectService,
                         PwaAppProcessingPermissionService appProcessingPermissionService,
                         CamundaWorkflowService camundaWorkflowService) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.appProcessingPermissionService = appProcessingPermissionService;
    this.applicationDetailSearcher = applicationDetailSearcher;
    this.pwaContactService = pwaContactService;
  }

  /**
   * Get work area items for user.
   */
  public PageView<PwaApplicationWorkAreaItem> getWorkAreaResultPage(AuthenticatedUserAccount authenticatedUserAccount,
                                                                    WorkAreaTab workAreaTab,
                                                                    int page) {

    var workAreaUri = ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, workAreaTab, page));

    // get tasks assigned to the user, grouped by workflow type
    Map<WorkflowType, List<AssignedTaskInstance>> workflowTypeToTaskMap = camundaWorkflowService
        .getAssignedTasks(authenticatedUserAccount.getLinkedPerson()).stream()
        .collect(Collectors.groupingBy(AssignedTaskInstance::getWorkflowType));

    // convert PWA app tasks into a list of business keys we can use to query apps
    Set<Integer> applicationIds = workflowTypeToTaskMap.entrySet().stream()
        .filter(entry -> entry.getKey().equals(WorkflowType.PWA_APPLICATION))
        .flatMap(entry -> entry.getValue().stream())
        .map(AssignedTaskInstance::getBusinessKey)
        .collect(Collectors.toSet());

    return PageView.fromPage(
        getApplicationSearchResults(authenticatedUserAccount, applicationIds, page),
        workAreaUri,
        sr -> new PwaApplicationWorkAreaItem(sr, this::viewApplicationUrlProducer)
    );

  }

  private Page<ApplicationDetailSearchItem> getApplicationSearchResults(WebUserAccount userAccount,
                                                                        Set<Integer> applicationIdList,
                                                                        int pageRequest) {

    var processingPermissions = appProcessingPermissionService.getProcessingPermissions(userAccount);

    // if the user doesn't have any processing permissions they're an industry user
    if (processingPermissions.isEmpty()) {
      return getIndustrySearchResults(userAccount, pageRequest);
    }

    var searchStatuses = new HashSet<PwaApplicationStatus>();

    if (processingPermissions.contains(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW)) {
      searchStatuses.add(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    }

    return applicationDetailSearcher.searchByStatusOrApplicationIds(
        getWorkAreaPageRequest(pageRequest, WorkAreaSort.PROPOSED_DATE_ASC),
        searchStatuses,
        applicationIdList
    );

  }

  private Page<ApplicationDetailSearchItem> getIndustrySearchResults(WebUserAccount userAccount, int pageRequest) {

    var applicationContactRoles = pwaContactService.getPwaContactRolesForWebUserAccount(
        userAccount,
        EnumSet.of(PwaContactRole.PREPARER));

    return applicationDetailSearcher.searchByPwaContacts(
        getWorkAreaPageRequest(pageRequest, WorkAreaSort.CREATED_DATE_DESC),
        applicationContactRoles
    );

  }

  private Pageable getWorkAreaPageRequest(int pageRequest, WorkAreaSort workAreaSort) {
    return PageRequest.of(pageRequest, PAGE_SIZE, workAreaSort.getSort());
  }

  private String viewApplicationUrlProducer(ApplicationDetailSearchItem applicationDetailSearchItem) {

    var applicationId = applicationDetailSearchItem.getPwaApplicationId();
    var applicationType = applicationDetailSearchItem.getApplicationType();

    switch (applicationDetailSearchItem.getPadStatus()) {

      case DRAFT:
        return pwaApplicationRedirectService.getTaskListRoute(applicationId, applicationType);
      case INITIAL_SUBMISSION_REVIEW:
        return ReverseRouter.route(on(InitialReviewController.class)
            .renderInitialReview(applicationId, applicationType, null, null, null));
      default:
        return ReverseRouter.route(on(CaseManagementController.class).renderCaseManagement(applicationId, applicationType, null, null));

    }

  }

}
