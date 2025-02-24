package uk.co.ogauthority.pwa.service.appprocessing.options;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowMessageEvents;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.GenericMessageEvent;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.consultations.WithdrawConsultationService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class OptionsCaseManagementWorkflowServiceTest {

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private WithdrawConsultationService withdrawConsultationService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private OptionsCaseManagementWorkflowService optionsCaseManagementWorkflowService;

  private PwaApplicationDetail pwaApplicationDetail;

  private AuthenticatedUserAccount authenticatedUserAccount;

  @BeforeEach
  void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(), Set.of());

    optionsCaseManagementWorkflowService = new OptionsCaseManagementWorkflowService(
        workflowAssignmentService,
        camundaWorkflowService,
        withdrawConsultationService,
        pwaApplicationDetailService);
  }

  @Test
  void doOptionsApprovedWorkflowUpdates_serviceInteractions() {

    optionsCaseManagementWorkflowService.doOptionsApprovalWork(pwaApplicationDetail);

    verify(workflowAssignmentService, times(1)).triggerWorkflowMessageAndAssertTaskExists(
        GenericMessageEvent.from(
            pwaApplicationDetail.getPwaApplication(),
            PwaApplicationWorkflowMessageEvents.OPTIONS_APPROVED.getMessageEventName()
        ),
        PwaApplicationWorkflowTask.UPDATE_APPLICATION
    );

    verifyNoMoreInteractions(
        workflowAssignmentService,
        camundaWorkflowService,
        withdrawConsultationService
    );

  }

  @Test
  void doOptionsCloseOutWorkFlowUpdates_serviceInteractions() {

    optionsCaseManagementWorkflowService.doCloseOutWork(pwaApplicationDetail, authenticatedUserAccount);

    verify(camundaWorkflowService).deleteProcessInstanceAndThenTasks(pwaApplicationDetail.getPwaApplication());
    verify(withdrawConsultationService).withdrawAllOpenConsultationRequests(pwaApplicationDetail.getPwaApplication(), authenticatedUserAccount);
    verify(pwaApplicationDetailService).updateStatus(pwaApplicationDetail, PwaApplicationStatus.COMPLETE, authenticatedUserAccount);

  }
}