package uk.co.ogauthority.pwa.service.appprocessing.options;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowMessageEvents;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.GenericMessageEvent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.consultations.WithdrawConsultationService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

/**
 * Handle all of the workflow marshalling required when doing options variation case processing.
 */
@Service
public class OptionsCaseManagementWorkflowService {

  private final WorkflowAssignmentService workflowAssignmentService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final WithdrawConsultationService withdrawConsultationService;
  private final PwaApplicationDetailService pwaApplicationDetailService;


  @Autowired
  public OptionsCaseManagementWorkflowService(WorkflowAssignmentService workflowAssignmentService,
                                              CamundaWorkflowService camundaWorkflowService,
                                              WithdrawConsultationService withdrawConsultationService,
                                              PwaApplicationDetailService pwaApplicationDetailService) {
    this.workflowAssignmentService = workflowAssignmentService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.withdrawConsultationService = withdrawConsultationService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }


  @Transactional
  public void doOptionsApprovalWork(PwaApplicationDetail pwaApplicationDetail) {
    workflowAssignmentService.triggerWorkflowMessageAndAssertTaskExists(
        GenericMessageEvent.from(
            pwaApplicationDetail.getPwaApplication(),
            PwaApplicationWorkflowMessageEvents.OPTIONS_APPROVED.getMessageEventName()
        ),
        PwaApplicationWorkflowTask.UPDATE_APPLICATION
    );
  }

  @Transactional
  public void doCloseOutWork(PwaApplicationDetail pwaApplicationDetail,
                             AuthenticatedUserAccount authenticatedUserAccount) {
    camundaWorkflowService.deleteProcessInstanceAndThenTasks(pwaApplicationDetail.getPwaApplication());
    withdrawConsultationService.withdrawAllOpenConsultationRequests(
        pwaApplicationDetail.getPwaApplication(),
        authenticatedUserAccount
    );
    pwaApplicationDetailService.updateStatus(pwaApplicationDetail, PwaApplicationStatus.COMPLETE, authenticatedUserAccount);

  }
}
