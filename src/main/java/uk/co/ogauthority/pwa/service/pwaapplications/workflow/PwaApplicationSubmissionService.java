package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDataCleanupService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

/**
 * Service to define and perform standardised application submission.
 */
@Service
public class PwaApplicationSubmissionService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final PwaApplicationDataCleanupService pwaApplicationDataCleanupService;


  private final ApplicationSubmissionServiceProvider applicationSubmissionServiceProvider;

  @Autowired
  public PwaApplicationSubmissionService(PwaApplicationDetailService pwaApplicationDetailService,
                                         CamundaWorkflowService camundaWorkflowService,
                                         PwaApplicationDataCleanupService pwaApplicationDataCleanupService,
                                         ApplicationSubmissionServiceProvider applicationSubmissionServiceProvider) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.pwaApplicationDataCleanupService = pwaApplicationDataCleanupService;
    this.applicationSubmissionServiceProvider = applicationSubmissionServiceProvider;

  }

  @Transactional
  public void submitApplication(WebUserAccount submittedByUser,
                                PwaApplicationDetail detail,
                                @Nullable String submissionDescription) {

    if (!detail.isTipFlag()) {
      throw new IllegalArgumentException(String.format("Application Detail not tip! id: %s", detail.getId()));
    }

    if (!detail.getStatus().equals(PwaApplicationStatus.DRAFT)) {
      throw new IllegalArgumentException(
          String.format("Application Detail not draft! id: %s status: %s", detail.getId(), detail.getStatus()));
    }

    var submissionService = applicationSubmissionServiceProvider.getSubmissionService(detail);

    submissionService.doBeforeSubmit(detail, submittedByUser.getLinkedPerson(), submissionDescription);

    pwaApplicationDataCleanupService.cleanupData(detail);

    submissionService.getSubmissionWorkflowResult()
        .ifPresent(pwaApplicationSubmitResult ->
            camundaWorkflowService.setWorkflowProperty(
                detail.getPwaApplication(),
                pwaApplicationSubmitResult
            )
        );

    camundaWorkflowService.completeTask(
        new WorkflowTaskInstance(detail.getPwaApplication(), submissionService.getTaskToComplete())
    );

    var newStatus = submissionService.getSubmittedApplicationDetailStatus(detail);
    pwaApplicationDetailService.setSubmitted(detail, submittedByUser, newStatus);

    submissionService.doAfterSubmit(detail);

  }

}
