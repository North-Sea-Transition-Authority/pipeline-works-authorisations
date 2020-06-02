package uk.co.ogauthority.pwa.service.appprocessing.initialreview;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

/**
 * Service to provide actions available to users at the 'Initial review' stage after submission.
 */
@Service
public class InitialReviewService {

  private final PwaApplicationDetailService applicationDetailService;
  private final CamundaWorkflowService workflowService;

  @Autowired
  public InitialReviewService(PwaApplicationDetailService applicationDetailService,
                              CamundaWorkflowService workflowService) {
    this.applicationDetailService = applicationDetailService;
    this.workflowService = workflowService;
  }

  @Transactional
  public void acceptApplication(PwaApplicationDetail detail,
                                WebUserAccount acceptingUser) {

    if (!detail.getStatus().equals(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)) {
      throw new ActionAlreadyPerformedException(
          String.format("Action: acceptApplication for app detail with ID: %s", detail.getId()));
    }

    applicationDetailService.setInitialReviewApproved(detail, acceptingUser);
    workflowService.completeTask(detail.getMasterPwaApplicationId(), UserWorkflowTask.APPLICATION_REVIEW);

  }

}
