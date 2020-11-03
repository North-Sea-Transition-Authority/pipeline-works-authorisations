package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Service to perform all submission business logic for pwa applications.
 */
@Service
public class ApplicationSubmissionServiceProvider {

  private final PwaApplicationFirstDraftSubmissionService pwaApplicationFirstDraftSubmissionService;
  private final PwaApplicationUpdateRequestedSubmissionService pwaApplicationUpdateRequestedSubmissionService;

  @Autowired
  public ApplicationSubmissionServiceProvider(
      PwaApplicationFirstDraftSubmissionService pwaApplicationFirstDraftSubmissionService,
      PwaApplicationUpdateRequestedSubmissionService pwaApplicationUpdateRequestedSubmissionService) {
    this.pwaApplicationFirstDraftSubmissionService = pwaApplicationFirstDraftSubmissionService;
    this.pwaApplicationUpdateRequestedSubmissionService = pwaApplicationUpdateRequestedSubmissionService;
  }


  public ApplicationSubmissionService getSubmissionService(PwaApplicationDetail pwaApplicationDetail) {

    if (pwaApplicationDetail.isFirstVersion()) {
      return pwaApplicationFirstDraftSubmissionService;
    }

    return pwaApplicationUpdateRequestedSubmissionService;

  }

}
