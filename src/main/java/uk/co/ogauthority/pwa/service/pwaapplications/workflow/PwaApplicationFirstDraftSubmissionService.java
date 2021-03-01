package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.ApplicationSubmittedEmailProps;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationSubmitResult;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.submission.PadPipelineNumberingService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;

/**
 * Service to perform all submission business logic for pwa applications.
 */
@Service
class PwaApplicationFirstDraftSubmissionService implements ApplicationSubmissionService {

  private final NotifyService notifyService;
  private final PwaTeamService pwaTeamService;
  private final PadPipelineNumberingService padPipelineNumberingService;


  @Autowired
  public PwaApplicationFirstDraftSubmissionService(NotifyService notifyService,
                                                   PwaTeamService pwaTeamService,
                                                   PadPipelineNumberingService padPipelineNumberingService) {

    this.notifyService = notifyService;
    this.pwaTeamService = pwaTeamService;
    this.padPipelineNumberingService = padPipelineNumberingService;
  }

  @Override
  public Optional<PwaApplicationSubmitResult> getSubmissionWorkflowResult() {
    return Optional.of(PwaApplicationSubmitResult.SUBMIT_PREPARED_APPLICATION);
  }

  @Override
  public PwaApplicationWorkflowTask getTaskToComplete() {
    return PwaApplicationWorkflowTask.PREPARE_APPLICATION;
  }

  @Override
  public PwaApplicationStatus getSubmittedApplicationDetailStatus(PwaApplicationDetail pwaApplicationDetail) {
    return PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW;
  }

  @Override
  public void doBeforeSubmit(PwaApplicationDetail pwaApplicationDetail, Person submittedByPerson, @Nullable String submissionDescription) {
    padPipelineNumberingService.assignPipelineReferences(pwaApplicationDetail);
  }

  @Override
  public void doAfterSubmit(PwaApplicationDetail pwaApplicationDetail) {
    sendApplicationSubmittedEmail(pwaApplicationDetail);
  }

  private void sendApplicationSubmittedEmail(PwaApplicationDetail detail) {

    var pwaManagers = pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER);

    pwaManagers.forEach(pwaManager -> {

      var submittedEmailProps = new ApplicationSubmittedEmailProps(
          pwaManager.getFullName(),
          detail.getPwaApplicationRef(),
          detail.getPwaApplicationType().getDisplayName());

      notifyService.sendEmail(submittedEmailProps, pwaManager.getEmailAddress());

    });

  }

}
