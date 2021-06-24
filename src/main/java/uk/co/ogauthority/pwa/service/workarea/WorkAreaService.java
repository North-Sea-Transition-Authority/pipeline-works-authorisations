package uk.co.ogauthority.pwa.service.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.workarea.applications.ApplicationWorkAreaSort;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.asbuilt.AsBuiltWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workflow.assignment.AssignmentService;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

@Service
public class WorkAreaService {

  public static final int PAGE_SIZE = 10;

  private final AsBuiltWorkAreaPageService asBuiltWorkAreaPageService;
  private final ConsultationWorkAreaPageService consultationWorkAreaPageService;
  private final AssignmentService assignmentService;
  private final ApplicationWorkAreaPageService applicationWorkAreaPageService;

  @Autowired
  public WorkAreaService(
      AsBuiltWorkAreaPageService asBuiltWorkAreaPageService,
      ConsultationWorkAreaPageService consultationWorkAreaPageService,
      AssignmentService assignmentService,
      ApplicationWorkAreaPageService applicationWorkAreaPageService) {
    this.asBuiltWorkAreaPageService = asBuiltWorkAreaPageService;
    this.consultationWorkAreaPageService = consultationWorkAreaPageService;
    this.assignmentService = assignmentService;
    this.applicationWorkAreaPageService = applicationWorkAreaPageService;
  }

  /**
   * Get work area items for user.
   */
  public WorkAreaResult getWorkAreaResult(WorkAreaContext workAreaContext,
                                          WorkAreaTab workAreaTab,
                                          int page) {

    var industryPageable = WorkAreaUtils.getWorkAreaPageRequest(page, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC);
    var regulatorPageable = WorkAreaUtils.getWorkAreaPageRequest(page, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC);

    String workAreaTabAndPageRoute;

    // Nice to have if time: services per tab to produce result object.
    switch (workAreaTab) {
      case INDUSTRY_OPEN_APPLICATIONS:
        workAreaTabAndPageRoute = ReverseRouter.route(
            on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS, page));
        return new WorkAreaResult(
            PageView.fromPage(
                applicationWorkAreaPageService.getUsersWorkAreaTabContents(
                    workAreaContext,
                    WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS.getWorkAreaTabCategory(),
                    industryPageable
                ),
                workAreaTabAndPageRoute,
                PwaApplicationWorkAreaItem::new
            ),
            null, null
        );

      case INDUSTRY_SUBMITTED_APPLICATIONS:
        workAreaTabAndPageRoute = ReverseRouter.route(
            on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS, page));
        return new WorkAreaResult(
            PageView.fromPage(
                applicationWorkAreaPageService.getUsersWorkAreaTabContents(
                    workAreaContext,
                    WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS.getWorkAreaTabCategory(),
                    industryPageable
                ),
                workAreaTabAndPageRoute,
                PwaApplicationWorkAreaItem::new
            ),
            null, null
        );

      case REGULATOR_REQUIRES_ATTENTION:
        workAreaTabAndPageRoute = ReverseRouter.route(
            on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, page));
        return new WorkAreaResult(
            PageView.fromPage(
                applicationWorkAreaPageService.getUsersWorkAreaTabContents(
                    workAreaContext,
                    WorkAreaTab.REGULATOR_REQUIRES_ATTENTION.getWorkAreaTabCategory(),
                    regulatorPageable
                ),
                workAreaTabAndPageRoute,
                PwaApplicationWorkAreaItem::new
            ),
            null, null
        );

      case REGULATOR_WAITING_ON_OTHERS:
        workAreaTabAndPageRoute = ReverseRouter.route(
            on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS, page));
        return new WorkAreaResult(
            PageView.fromPage(
                applicationWorkAreaPageService.getUsersWorkAreaTabContents(
                    workAreaContext,
                    WorkAreaTab.REGULATOR_WAITING_ON_OTHERS.getWorkAreaTabCategory(),
                    regulatorPageable
                ),
                workAreaTabAndPageRoute,
                PwaApplicationWorkAreaItem::new
            ),
            null, null
        );

      case OPEN_CONSULTATIONS:
        Map<WorkflowType, List<Assignment>> workflowTypeToAssignmentMap = assignmentService.getAssignmentsForPerson(
            workAreaContext.getAuthenticatedUserAccount().getLinkedPerson()
        );
        var businessKeys = getBusinessKeysFromWorkflowToTaskMap(workflowTypeToAssignmentMap,
            WorkflowType.PWA_APPLICATION_CONSULTATION);
        return new WorkAreaResult(null,
            consultationWorkAreaPageService.getPageView(workAreaContext.getAuthenticatedUserAccount(), businessKeys,
                page),
            null);

      case AS_BUILT_NOTIFICATIONS:
        return new WorkAreaResult(null,
            null,
            asBuiltWorkAreaPageService.getAsBuiltNotificationsPageView(workAreaContext.getAuthenticatedUserAccount(),
                page));

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

}
