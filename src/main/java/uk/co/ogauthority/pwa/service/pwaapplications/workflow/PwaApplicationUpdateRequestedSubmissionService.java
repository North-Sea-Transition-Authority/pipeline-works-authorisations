package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationSubmitResult;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;

/**
 * Service to to customise submission behaviour when an update request is responded to.
 */
@Service
class PwaApplicationUpdateRequestedSubmissionService implements ApplicationSubmissionService {

  private final ApplicationUpdateRequestService applicationUpdateRequestService;
  private final ApplicationInvolvementService applicationInvolvementService;

  @Autowired
  public PwaApplicationUpdateRequestedSubmissionService(ApplicationUpdateRequestService applicationUpdateRequestService,
                                                        ApplicationInvolvementService applicationInvolvementService) {

    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.applicationInvolvementService = applicationInvolvementService;
  }

  @Override
  public Optional<PwaApplicationSubmitResult> getSubmissionWorkflowResult() {
    return Optional.empty();
  }

  @Override
  public PwaApplicationWorkflowTask getTaskToComplete() {
    return PwaApplicationWorkflowTask.UPDATE_APPLICATION;
  }

  @Override
  public PwaApplicationStatus getSubmittedApplicationDetailStatus(PwaApplicationDetail pwaApplicationDetail) {
    var caseOfficerPersonIdOpt = applicationInvolvementService.getCaseOfficerPersonId(
        pwaApplicationDetail.getPwaApplication());

    if (caseOfficerPersonIdOpt.isPresent()) {
      return PwaApplicationStatus.CASE_OFFICER_REVIEW;
    }

    return PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW;

  }

  @Override
  public void doBeforeSubmit(PwaApplicationDetail pwaApplicationDetail, Person submittedByPerson,
                             @Nullable String submissionDescription) {
    applicationUpdateRequestService.respondToApplicationOpenUpdateRequest(pwaApplicationDetail, submittedByPerson,
        submissionDescription);
  }

  @Override
  public void doAfterSubmit(PwaApplicationDetail pwaApplicationDetail) {
    // nothing to do here
  }

}
