package uk.co.ogauthority.pwa.service.workarea.applications;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.toSet;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.CaseManagementController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.model.workflow.WorkflowBusinessKey;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaApplicationContactRoleDto;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

@Service
public class ApplicationWorkAreaPageService {

  private final PwaAppProcessingPermissionService appProcessingPermissionService;
  private final ApplicationDetailSearcher applicationDetailSearcher;
  private final PwaContactService pwaContactService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final UserTypeService userTypeService;

  @Autowired
  public ApplicationWorkAreaPageService(PwaAppProcessingPermissionService appProcessingPermissionService,
                                        ApplicationDetailSearcher applicationDetailSearcher,
                                        PwaContactService pwaContactService,
                                        PwaApplicationRedirectService pwaApplicationRedirectService,
                                        CamundaWorkflowService camundaWorkflowService,
                                        UserTypeService userTypeService) {

    this.appProcessingPermissionService = appProcessingPermissionService;
    this.applicationDetailSearcher = applicationDetailSearcher;
    this.pwaContactService = pwaContactService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.userTypeService = userTypeService;
  }

  public PageView<PwaApplicationWorkAreaItem> getPageView(AuthenticatedUserAccount authenticatedUserAccount,
                                                          Set<Integer> applicationIds,
                                                          int page) {

    var workAreaUri = ReverseRouter.route(
        on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.OPEN_APPLICATIONS, page));

    return PageView.fromPage(
        getApplicationSearchResults(authenticatedUserAccount, applicationIds, page),
        workAreaUri,
        sr -> new PwaApplicationWorkAreaItem(sr, this::viewApplicationUrlProducer)
    );

  }

  private Page<ApplicationDetailSearchItem> getApplicationSearchResults(AuthenticatedUserAccount userAccount,
                                                                        Set<Integer> applicationIdList,
                                                                        int pageRequest) {

    var userType = userTypeService.getUserType(userAccount);

    if (userType == UserType.INDUSTRY) {
      return getIndustrySearchResults(userAccount, pageRequest);
    }

    var searchStatuses = new HashSet<PwaApplicationStatus>();

    var processingPermissions = appProcessingPermissionService.getGenericProcessingPermissions(userAccount);

    if (processingPermissions.contains(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW)) {
      searchStatuses.add(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    }

    return applicationDetailSearcher.searchByStatusOrApplicationIds(
        WorkAreaUtils.getWorkAreaPageRequest(pageRequest, ApplicationWorkAreaSort.PROPOSED_DATE_ASC),
        searchStatuses,
        applicationIdList
    );

  }

  private Page<ApplicationDetailSearchItem> getIndustrySearchResults(WebUserAccount userAccount, int pageRequest) {

    return applicationDetailSearcher.searchByStatusOrApplicationIds(
        WorkAreaUtils.getWorkAreaPageRequest(pageRequest, ApplicationWorkAreaSort.CREATED_DATE_DESC),
        Set.of(),
        getIndustryUserApplicationIds(userAccount)
    );

  }

  private Set<Integer> getIndustryUserApplicationIds(WebUserAccount webUserAccount) {
    var applicationContactRoles = pwaContactService.getPwaContactRolesForWebUserAccount(
        webUserAccount,
        EnumSet.of(PwaContactRole.PREPARER));

    var targetBusinessKeys = applicationContactRoles.stream()
        .map(PwaApplicationContactRoleDto::getPwaApplicationId)
        .map(WorkflowBusinessKey::from)
        .collect(toSet());

    return camundaWorkflowService.filterBusinessKeysByWorkflowTypeAndActiveTasksContains(
        WorkflowType.PWA_APPLICATION,
        targetBusinessKeys,
        Set.of(
            PwaApplicationWorkflowTask.PREPARE_APPLICATION,
            PwaApplicationWorkflowTask.AWAIT_FEEDBACK,
            PwaApplicationWorkflowTask.UPDATE_APPLICATION)

    ).stream()
        .map(workflowBusinessKey -> Integer.valueOf(workflowBusinessKey.getValue()))
        .collect(toImmutableSet());

  }

  private String viewApplicationUrlProducer(ApplicationDetailSearchItem applicationDetailSearchItem) {

    var applicationId = applicationDetailSearchItem.getPwaApplicationId();
    var applicationType = applicationDetailSearchItem.getApplicationType();

    if (applicationDetailSearchItem.getPadStatus() == PwaApplicationStatus.DRAFT) {
      return pwaApplicationRedirectService.getTaskListRoute(applicationId, applicationType);
    }

    return ReverseRouter.route(on(CaseManagementController.class)
        .renderCaseManagement(applicationId, applicationType, AppProcessingTab.TASKS, null, null));

  }

}
