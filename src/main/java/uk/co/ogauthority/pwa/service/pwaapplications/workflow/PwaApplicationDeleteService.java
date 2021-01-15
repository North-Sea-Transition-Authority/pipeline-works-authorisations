package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

/**
 * Service to delete pwa applications.
 */
@Service
public class PwaApplicationDeleteService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final CamundaWorkflowService camundaWorkflowService;


  @Autowired
  public PwaApplicationDeleteService(PwaApplicationDetailService pwaApplicationDetailService,
                                     CamundaWorkflowService camundaWorkflowService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.camundaWorkflowService = camundaWorkflowService;

  }

  @Transactional
  public void deleteApplication(WebUserAccount submittedByUser,
                                PwaApplicationDetail pwaApplicationDetail) {

    if (!pwaApplicationDetail.isTipFlag()) {
      throw new IllegalArgumentException(String.format("Application Detail not tip! id: %s", pwaApplicationDetail.getId()));
    }

    if (!pwaApplicationDetail.getStatus().equals(PwaApplicationStatus.DRAFT)) {
      throw new IllegalArgumentException(
          String.format("Application Detail not draft! id: %s status: %s", pwaApplicationDetail.getId(), pwaApplicationDetail.getStatus()));
    }


    pwaApplicationDetailService.setDeleted(pwaApplicationDetail, submittedByUser.getLinkedPerson());
    camundaWorkflowService.deleteProcessInstanceAndThenTasks(pwaApplicationDetail.getPwaApplication());


  }


}
