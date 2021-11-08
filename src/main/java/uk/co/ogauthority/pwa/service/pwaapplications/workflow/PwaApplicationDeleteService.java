package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.ApplicationDeletionException;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
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

    if (!pwaApplicationDetailService.applicationDetailCanBeDeleted(pwaApplicationDetail)) {
      throw new ApplicationDeletionException("Cannot delete application detail with id:" + pwaApplicationDetail.getId());
    }

    pwaApplicationDetailService.setDeleted(pwaApplicationDetail, submittedByUser.getLinkedPerson());
    camundaWorkflowService.deleteProcessInstanceAndThenTasks(pwaApplicationDetail.getPwaApplication());


  }


}
