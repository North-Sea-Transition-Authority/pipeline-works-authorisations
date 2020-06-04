package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

/**
 * Service to perform all submission business logic for pwa applications.
 */
@Service
public class PwaApplicationSubmissionService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final CamundaWorkflowService camundaWorkflowService;

  @Autowired
  public PwaApplicationSubmissionService(PwaApplicationDetailService pwaApplicationDetailService,
                                         CamundaWorkflowService camundaWorkflowService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.camundaWorkflowService = camundaWorkflowService;
  }

  @Transactional
  public void submitApplication(WebUserAccount submittedByUser, PwaApplicationDetail detail) {

    if (!detail.isTipFlag()) {
      throw new IllegalArgumentException(String.format("Application Detail not tip! id: %s", detail.getId()));
    }

    if (!detail.getStatus().equals(PwaApplicationStatus.DRAFT)) {
      throw new IllegalArgumentException(
          String.format("Application Detail not draft! id: %s status: %s", detail.getId(), detail.getStatus()));
    }

    camundaWorkflowService.completeTask(
        new WorkflowTaskInstance(detail.getPwaApplication(), PwaApplicationWorkflowTask.PREPARE_APPLICATION));
    pwaApplicationDetailService.setSubmitted(detail, submittedByUser);

  }

}
