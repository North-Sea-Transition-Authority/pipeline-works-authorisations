package uk.co.ogauthority.pwa.service.workarea.applications;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.toSet;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.workflow.WorkflowBusinessKey;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaApplicationContactRoleDto;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

@Service
public class IndustryWorkAreaPageService {

  private static final Set<PwaApplicationWorkflowTask> ALL_APP_CONTACT_WORKFLOW_TASKS = EnumSet.of(
      PwaApplicationWorkflowTask.PREPARE_APPLICATION,
      PwaApplicationWorkflowTask.AWAIT_FEEDBACK,
      PwaApplicationWorkflowTask.UPDATE_APPLICATION
  );

  private final WorkAreaApplicationDetailSearcher workAreaApplicationDetailSearcher;
  private final PwaContactService pwaContactService;
  private final CamundaWorkflowService camundaWorkflowService;

  @Autowired
  public IndustryWorkAreaPageService(WorkAreaApplicationDetailSearcher workAreaApplicationDetailSearcher,
                                     PwaContactService pwaContactService,
                                     CamundaWorkflowService camundaWorkflowService) {
    this.workAreaApplicationDetailSearcher = workAreaApplicationDetailSearcher;
    this.pwaContactService = pwaContactService;
    this.camundaWorkflowService = camundaWorkflowService;
  }

  public PageView<PwaApplicationWorkAreaItem> getOpenApplicationsPageView(
      AuthenticatedUserAccount authenticatedUserAccount,
      int page) {

    var workAreaUri = ReverseRouter.route(
        on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS, page));

    var applicationIdFilter = getBusinessKeysWhereUserIsAppPreparerAndTaskActive(authenticatedUserAccount, ALL_APP_CONTACT_WORKFLOW_TASKS);

    return PageView.fromPage(
        workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(
            WorkAreaUtils.getWorkAreaPageRequest(page, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
            applicationIdFilter,
            ApplicationState.REQUIRES_INDUSTRY_ATTENTION.getStatuses(),
            true
        ),
        workAreaUri,
        PwaApplicationWorkAreaItem::new
    );

  }

  public PageView<PwaApplicationWorkAreaItem> getSubmittedApplicationsPageView(
      AuthenticatedUserAccount authenticatedUserAccount,
      int page) {

    var workAreaUri = ReverseRouter.route(
        on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS, page));

    var applicationIdFilter = getBusinessKeysWhereUserIsAppPreparerAndTaskActive(
        authenticatedUserAccount,
        ALL_APP_CONTACT_WORKFLOW_TASKS);
    var notOpenApplicationStatusFilter =  EnumSet.complementOf(EnumSet.copyOf(ApplicationState.REQUIRES_INDUSTRY_ATTENTION.getStatuses()));

    return PageView.fromPage(
        workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInAndOpenUpdateRequest(
            WorkAreaUtils.getWorkAreaPageRequest(page, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
            applicationIdFilter,
            notOpenApplicationStatusFilter,
            false
        ),
        workAreaUri,
        PwaApplicationWorkAreaItem::new
    );

  }

  public Set<Integer> getBusinessKeysWhereUserIsAppPreparerAndTaskActive(WebUserAccount webUserAccount,
                                                                         Set<PwaApplicationWorkflowTask> applicationWorkflowTasks) {

    var applicationContactRoles = pwaContactService.getPwaContactRolesForWebUserAccount(
        webUserAccount,
        EnumSet.of(PwaContactRole.PREPARER));

    var targetBusinessKeys = applicationContactRoles.stream()
        .map(PwaApplicationContactRoleDto::getPwaApplicationId)
        .map(WorkflowBusinessKey::from)
        .collect(toSet());

    // convert to generic workflow task Set.
    Set<UserWorkflowTask> userTasks = applicationWorkflowTasks.stream()
        .collect(Collectors.toUnmodifiableSet());

    return camundaWorkflowService.filterBusinessKeysByWorkflowTypeAndActiveTasksContains(
        WorkflowType.PWA_APPLICATION,
        targetBusinessKeys,
        userTasks
    )
        .stream()
        .map(workflowBusinessKey -> Integer.valueOf(workflowBusinessKey.getValue()))
        .collect(toImmutableSet());

  }

}
