package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.ApplicationSubmissionException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * Service to perform all submission business logic for pwa applications.
 */
@Service
public class ApplicationSubmissionServiceProvider {

  private final PwaApplicationFirstDraftSubmissionService pwaApplicationFirstDraftSubmissionService;
  private final PwaApplicationUpdateRequestedSubmissionService pwaApplicationUpdateRequestedSubmissionService;
  private final ApplicationUpdateRequestService applicationUpdateRequestService;
  private final PwaApplicationOptionConfirmationSubmissionService pwaApplicationOptionConfirmationSubmissionService;

  @Autowired
  public ApplicationSubmissionServiceProvider(
      PwaApplicationFirstDraftSubmissionService pwaApplicationFirstDraftSubmissionService,
      PwaApplicationUpdateRequestedSubmissionService pwaApplicationUpdateRequestedSubmissionService,
      ApplicationUpdateRequestService applicationUpdateRequestService,
      PwaApplicationOptionConfirmationSubmissionService pwaApplicationOptionConfirmationSubmissionService) {
    this.pwaApplicationFirstDraftSubmissionService = pwaApplicationFirstDraftSubmissionService;
    this.pwaApplicationUpdateRequestedSubmissionService = pwaApplicationUpdateRequestedSubmissionService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.pwaApplicationOptionConfirmationSubmissionService = pwaApplicationOptionConfirmationSubmissionService;
  }


  public ApplicationSubmissionService getSubmissionService(PwaApplicationDetail pwaApplicationDetail) {

    if (pwaApplicationDetail.isFirstVersion()) {
      return pwaApplicationFirstDraftSubmissionService;
    }

    if (applicationUpdateRequestService.applicationDetailHasOpenUpdateRequest(pwaApplicationDetail)) {
      return pwaApplicationUpdateRequestedSubmissionService;
    }

    if (!pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION)) {
      throw new ApplicationSubmissionException(
          "Only Options variation applications should reach this point. pad.id:" + pwaApplicationDetail.getId()
      );
    }

    return pwaApplicationOptionConfirmationSubmissionService;


  }

}
