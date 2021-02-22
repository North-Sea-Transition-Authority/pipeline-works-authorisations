package uk.co.ogauthority.pwa.service.workarea;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.workarea.applications.IndustryWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workarea.applications.RegulatorWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.AssignedTaskInstance;

@Service
public class WorkAreaService {

  public static final int PAGE_SIZE = 10;

  private final CamundaWorkflowService camundaWorkflowService;
  private final IndustryWorkAreaPageService industryWorkAreaPageService;
  private final ConsultationWorkAreaPageService consultationWorkAreaPageService;
  private final RegulatorWorkAreaPageService regulatorWorkAreaPageService;
  private final PublicNoticeService publicNoticeService;

  @Autowired
  public WorkAreaService(CamundaWorkflowService camundaWorkflowService,
                         IndustryWorkAreaPageService industryWorkAreaPageService,
                         ConsultationWorkAreaPageService consultationWorkAreaPageService,
                         RegulatorWorkAreaPageService regulatorWorkAreaPageService,
                         PublicNoticeService publicNoticeService) {
    this.camundaWorkflowService = camundaWorkflowService;
    this.industryWorkAreaPageService = industryWorkAreaPageService;
    this.consultationWorkAreaPageService = consultationWorkAreaPageService;
    this.regulatorWorkAreaPageService = regulatorWorkAreaPageService;
    this.publicNoticeService = publicNoticeService;
  }

  /**
   * Get work area items for user.
   */
  public WorkAreaResult getWorkAreaResult(AuthenticatedUserAccount authenticatedUserAccount,
                                          WorkAreaTab workAreaTab,
                                          int page) {

    // get tasks assigned to the user, grouped by workflow type
    Map<WorkflowType, List<AssignedTaskInstance>> workflowTypeToTaskMap = camundaWorkflowService
        .getAssignedTasks(authenticatedUserAccount.getLinkedPerson()).stream()
        .collect(Collectors.groupingBy(AssignedTaskInstance::getWorkflowType));

    Set<Integer> businessKeys;

    switch (workAreaTab) {

      case INDUSTRY_OPEN_APPLICATIONS:
        return new WorkAreaResult(
            industryWorkAreaPageService.getOpenApplicationsPageView(authenticatedUserAccount, page),
            null
        );

      case INDUSTRY_SUBMITTED_APPLICATIONS:
        return new WorkAreaResult(
            industryWorkAreaPageService.getSubmittedApplicationsPageView(authenticatedUserAccount, page),
            null
        );

      case REGULATOR_REQUIRES_ATTENTION:
        businessKeys = getBusinessKeysFromWorkflowToTaskMap(workflowTypeToTaskMap, WorkflowType.PWA_APPLICATION);
        if (authenticatedUserAccount.getUserPrivileges().contains(PwaUserPrivilege.PWA_MANAGER)) {
          businessKeys.addAll(getApplicationIdsForOpenPublicNotices());
        }
        return new WorkAreaResult(
            regulatorWorkAreaPageService.getRequiresAttentionPageView(authenticatedUserAccount, businessKeys, page),
            null
        );

      case REGULATOR_WAITING_ON_OTHERS:
        businessKeys = getBusinessKeysFromWorkflowToTaskMap(workflowTypeToTaskMap, WorkflowType.PWA_APPLICATION);
        return new WorkAreaResult(
            regulatorWorkAreaPageService.getWaitingOnOthersPageView(authenticatedUserAccount, businessKeys, page),
            null
        );

      case OPEN_CONSULTATIONS:
        businessKeys = getBusinessKeysFromWorkflowToTaskMap(workflowTypeToTaskMap,
            WorkflowType.PWA_APPLICATION_CONSULTATION);
        return new WorkAreaResult(null,
            consultationWorkAreaPageService.getPageView(authenticatedUserAccount, businessKeys, page));

      default:
        throw new RuntimeException(String.format(
            "Work area tab page provider not implemented for tab: %s", workAreaTab.name()));

    }

  }

  /**
   * Retrieve business keys for assigned tasks that are in the requested workflow type.
   */
  private Set<Integer> getBusinessKeysFromWorkflowToTaskMap(
      Map<WorkflowType, List<AssignedTaskInstance>> workflowToTaskMap,
      WorkflowType workflowType) {

    return workflowToTaskMap.entrySet().stream()
        .filter(entry -> entry.getKey().equals(workflowType))
        .flatMap(entry -> entry.getValue().stream())
        .map(AssignedTaskInstance::getBusinessKey)
        .collect(Collectors.toSet());

  }

  private Set<Integer> getApplicationIdsForOpenPublicNotices() {
    return publicNoticeService.getOpenPublicNoticesByStatus(PublicNoticeStatus.MANAGER_APPROVAL)
        .stream().map(publicNotice -> publicNotice.getPwaApplication().getId())
        .collect(Collectors.toSet());
  }

}
