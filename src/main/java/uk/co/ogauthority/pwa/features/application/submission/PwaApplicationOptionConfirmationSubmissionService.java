package uk.co.ogauthority.pwa.features.application.submission;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationSubmitResult;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

/**
 * Service to to customise submission behaviour when an update request is responded to.
 */
@Service
class PwaApplicationOptionConfirmationSubmissionService implements ApplicationSubmissionService {

  private final ApplicationInvolvementService applicationInvolvementService;
  private final PadPipelineNumberingService padPipelineNumberingService;

  @Autowired
  public PwaApplicationOptionConfirmationSubmissionService(ApplicationInvolvementService applicationInvolvementService,
                                                           PadPipelineNumberingService padPipelineNumberingService) {

    this.applicationInvolvementService = applicationInvolvementService;
    this.padPipelineNumberingService = padPipelineNumberingService;
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
  public void doBeforeSubmit(PwaApplicationDetail pwaApplicationDetail, Person submittedByPerson, @Nullable String submissionDescription) {
    padPipelineNumberingService.assignPipelineReferences(pwaApplicationDetail);
  }

  @Override
  public void doAfterSubmit(PwaApplicationDetail pwaApplicationDetail) {
    // nothing to do here
  }

  @Override
  public ApplicationSubmissionType getSubmissionType() {
    return ApplicationSubmissionType.OPTIONS_CONFIRMATION;
  }

}
