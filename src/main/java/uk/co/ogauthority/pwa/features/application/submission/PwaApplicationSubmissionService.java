package uk.co.ogauthority.pwa.features.application.submission;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationSubmitResult;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

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

    if (!ApplicationState.INDUSTRY_EDITABLE.includes(detail.getStatus())) {
      throw new IllegalArgumentException(
          String.format("Application Detail not industry editable! id: %s status: %s", detail.getId(), detail.getStatus()));
    }

    var submissionService = applicationSubmissionServiceProvider.getSubmissionService(detail);

    submissionService.doBeforeSubmit(detail, submittedByUser.getLinkedPerson(), submissionDescription);

    pwaApplicationDataCleanupService.cleanupData(detail, submittedByUser);

    submissionService.getSubmissionWorkflowResult().ifPresent(
        pwaApplicationSubmitResult -> setWorkFlowProperty(detail, pwaApplicationSubmitResult)
    );

    camundaWorkflowService.completeTask(
        new WorkflowTaskInstance(detail.getPwaApplication(), submissionService.getTaskToComplete())
    );

    var newStatus = submissionService.getSubmittedApplicationDetailStatus(detail);
    pwaApplicationDetailService.setSubmitted(detail, submittedByUser, newStatus);

    submissionService.doAfterSubmit(detail);

  }

  // this is a workaround to avoid crap checkstyle false positive build failures.
  private void setWorkFlowProperty(PwaApplicationDetail detail, PwaApplicationSubmitResult pwaApplicationSubmitResult) {
    camundaWorkflowService.setWorkflowProperty(
        detail.getPwaApplication(),
        pwaApplicationSubmitResult
    );
  }

}
