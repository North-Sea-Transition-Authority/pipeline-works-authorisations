package uk.co.ogauthority.pwa.service.workarea;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.AssignedTaskInstance;

@Service
public class WorkAreaService {

  public static final int PAGE_SIZE = 10;

  private final CamundaWorkflowService camundaWorkflowService;
  private final ApplicationWorkAreaPageService applicationWorkAreaPageService;


  @Autowired
  public WorkAreaService(CamundaWorkflowService camundaWorkflowService,
                         ApplicationWorkAreaPageService applicationWorkAreaPageService) {
    this.camundaWorkflowService = camundaWorkflowService;
    this.applicationWorkAreaPageService = applicationWorkAreaPageService;
  }

  /**
   * Get work area items for user.
   */
  public PageView<PwaApplicationWorkAreaItem> getWorkAreaResultPage(AuthenticatedUserAccount authenticatedUserAccount,
                                                                    WorkAreaTab workAreaTab,
                                                                    int page) {

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

    return applicationWorkAreaPageService.getPageView(authenticatedUserAccount, applicationIds, page);

  }

}
