package uk.co.ogauthority.pwa.service.workarea;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.workarea.applications.IndustryWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workarea.applications.RegulatorWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workarea.asbuilt.AsBuiltWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workflow.assignment.AssignmentService;

@Service
public class WorkAreaService {

  public static final int PAGE_SIZE = 10;

  private final AsBuiltWorkAreaPageService asBuiltWorkAreaPageService;
  private final IndustryWorkAreaPageService industryWorkAreaPageService;
  private final ConsultationWorkAreaPageService consultationWorkAreaPageService;
  private final RegulatorWorkAreaPageService regulatorWorkAreaPageService;
  private final PublicNoticeService publicNoticeService;
  private final AssignmentService assignmentService;

  @Autowired
  public WorkAreaService(
      AsBuiltWorkAreaPageService asBuiltWorkAreaPageService,
      IndustryWorkAreaPageService industryWorkAreaPageService,
      ConsultationWorkAreaPageService consultationWorkAreaPageService,
      RegulatorWorkAreaPageService regulatorWorkAreaPageService,
      PublicNoticeService publicNoticeService,
      AssignmentService assignmentService) {
    this.asBuiltWorkAreaPageService = asBuiltWorkAreaPageService;
    this.industryWorkAreaPageService = industryWorkAreaPageService;
    this.consultationWorkAreaPageService = consultationWorkAreaPageService;
    this.regulatorWorkAreaPageService = regulatorWorkAreaPageService;
    this.publicNoticeService = publicNoticeService;
    this.assignmentService = assignmentService;
  }

  /**
   * Get work area items for user.
   */
  public WorkAreaResult getWorkAreaResult(AuthenticatedUserAccount authenticatedUserAccount,
                                          WorkAreaTab workAreaTab,
                                          int page) {

    Map<WorkflowType, List<Assignment>> workflowTypeToAssignmentMap = assignmentService.getAssignmentsForPerson(
        authenticatedUserAccount.getLinkedPerson());

    Set<Integer> businessKeys;

    switch (workAreaTab) {

      case INDUSTRY_OPEN_APPLICATIONS:
        return new WorkAreaResult(
            industryWorkAreaPageService.getOpenApplicationsPageView(authenticatedUserAccount, page),
            null,
            null);

      case INDUSTRY_SUBMITTED_APPLICATIONS:
        return new WorkAreaResult(
            industryWorkAreaPageService.getSubmittedApplicationsPageView(authenticatedUserAccount, page),
            null,
            null);

      case REGULATOR_REQUIRES_ATTENTION:
        businessKeys = getBusinessKeysFromWorkflowToTaskMap(workflowTypeToAssignmentMap, WorkflowType.PWA_APPLICATION);
        if (authenticatedUserAccount.getUserPrivileges().contains(PwaUserPrivilege.PWA_MANAGER)) {
          businessKeys.addAll(getApplicationIdsForOpenPublicNotices());
        }
        if (authenticatedUserAccount.hasPrivilege(PwaUserPrivilege.PWA_INDUSTRY)) {
          businessKeys.addAll(
              industryWorkAreaPageService.getBusinessKeysWhereUserIsAppPreparerAndTaskActive(
                  authenticatedUserAccount,
                  EnumSet.of(PwaApplicationWorkflowTask.PREPARE_APPLICATION,
                      PwaApplicationWorkflowTask.UPDATE_APPLICATION))
          );
        }

        return new WorkAreaResult(
            regulatorWorkAreaPageService.getRequiresAttentionPageView(authenticatedUserAccount, businessKeys, page),
            null,
            null);

      case REGULATOR_WAITING_ON_OTHERS:
        businessKeys = getBusinessKeysFromWorkflowToTaskMap(workflowTypeToAssignmentMap, WorkflowType.PWA_APPLICATION);
        return new WorkAreaResult(
            regulatorWorkAreaPageService.getWaitingOnOthersPageView(authenticatedUserAccount, businessKeys, page),
            null,
            null);

      case OPEN_CONSULTATIONS:
        businessKeys = getBusinessKeysFromWorkflowToTaskMap(workflowTypeToAssignmentMap, WorkflowType.PWA_APPLICATION_CONSULTATION);
        return new WorkAreaResult(null,
            consultationWorkAreaPageService.getPageView(authenticatedUserAccount, businessKeys, page),
            null);

      case AS_BUILT_NOTIFICATIONS:
        return new WorkAreaResult(null,
            null,
            asBuiltWorkAreaPageService.getAsBuiltNotificationsPageView(authenticatedUserAccount, page));

      default:
        throw new RuntimeException(String.format(
            "Work area tab page provider not implemented for tab: %s", workAreaTab.name()));

    }

  }

  /**
   * Retrieve business keys for assigned tasks that are in the requested workflow type.
   */
  private Set<Integer> getBusinessKeysFromWorkflowToTaskMap(
      Map<WorkflowType, List<Assignment>> workflowToAssignmentMap,
      WorkflowType workflowType) {

    // todo ensure apps returned are still in progress, PWA-177 after ending workflows, clear assignments in the Assignment table
    return workflowToAssignmentMap
        .getOrDefault(workflowType, List.of())
        .stream()
        .map(Assignment::getBusinessKey)
        .collect(Collectors.toSet());

  }

  private Set<Integer> getApplicationIdsForOpenPublicNotices() {
    return publicNoticeService.getOpenPublicNotices()
        .stream().map(publicNotice -> publicNotice.getPwaApplication().getId())
        .collect(Collectors.toSet());
  }

}
