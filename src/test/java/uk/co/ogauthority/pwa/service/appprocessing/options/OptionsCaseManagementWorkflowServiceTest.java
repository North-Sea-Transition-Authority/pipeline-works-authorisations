package uk.co.ogauthority.pwa.service.appprocessing.options;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.workflow.GenericMessageEvent;
import uk.co.ogauthority.pwa.service.consultations.WithdrawConsultationService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowMessageEvents;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class OptionsCaseManagementWorkflowServiceTest {

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

  @Before
  public void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(), Set.of());

    optionsCaseManagementWorkflowService = new OptionsCaseManagementWorkflowService(
        workflowAssignmentService,
        camundaWorkflowService,
        withdrawConsultationService,
        pwaApplicationDetailService);
  }

  @Test
  public void doOptionsApprovedWorkflowUpdates_serviceInteractions() {

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
  public void doOptionsCloseOutWorkFlowUpdates_serviceInteractions() {

    optionsCaseManagementWorkflowService.doCloseOutWork(pwaApplicationDetail, authenticatedUserAccount);

    verify(camundaWorkflowService).deleteProcessInstanceAndThenTasks(pwaApplicationDetail.getPwaApplication());
    verify(withdrawConsultationService).withdrawAllOpenConsultationRequests(pwaApplicationDetail.getPwaApplication(), authenticatedUserAccount);
    verify(pwaApplicationDetailService).updateStatus(pwaApplicationDetail, PwaApplicationStatus.COMPLETE, authenticatedUserAccount);

  }
}