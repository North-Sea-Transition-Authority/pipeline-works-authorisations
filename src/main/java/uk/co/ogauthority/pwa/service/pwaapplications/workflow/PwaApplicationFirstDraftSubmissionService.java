package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.email.EmailCaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ApplicationSubmittedEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationSubmitResult;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
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
  private final EmailCaseLinkService emailCaseLinkService;


  @Autowired
  public PwaApplicationFirstDraftSubmissionService(NotifyService notifyService,
                                                   PwaTeamService pwaTeamService,
                                                   PadPipelineNumberingService padPipelineNumberingService,
                                                   EmailCaseLinkService emailCaseLinkService) {

    this.notifyService = notifyService;
    this.pwaTeamService = pwaTeamService;
    this.padPipelineNumberingService = padPipelineNumberingService;
    this.emailCaseLinkService = emailCaseLinkService;
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
          detail.getPwaApplicationType().getDisplayName(),
          emailCaseLinkService.generateCaseManagementLink(detail.getPwaApplication()));

      notifyService.sendEmail(submittedEmailProps, pwaManager.getEmailAddress());

    });

  }

}
