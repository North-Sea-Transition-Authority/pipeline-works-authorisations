package uk.co.ogauthority.pwa.features.application.submission;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationSubmitResult;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ApplicationSubmittedEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

/**
 * Service to perform all submission business logic for pwa applications.
 */
@Service
class PwaApplicationFirstDraftSubmissionService implements ApplicationSubmissionService {

  private final NotifyService notifyService;
  private final PadPipelineNumberingService padPipelineNumberingService;
  private final CaseLinkService caseLinkService;
  private final TeamQueryService teamQueryService;


  @Autowired
  public PwaApplicationFirstDraftSubmissionService(NotifyService notifyService,
                                                   PadPipelineNumberingService padPipelineNumberingService,
                                                   CaseLinkService caseLinkService,
                                                   TeamQueryService teamQueryService) {

    this.notifyService = notifyService;
    this.padPipelineNumberingService = padPipelineNumberingService;
    this.caseLinkService = caseLinkService;
    this.teamQueryService = teamQueryService;
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

  @Override
  public ApplicationSubmissionType getSubmissionType() {
    return ApplicationSubmissionType.FIRST_DRAFT;
  }

  private void sendApplicationSubmittedEmail(PwaApplicationDetail detail) {

    var pwaManagers = teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, Role.PWA_MANAGER);

    pwaManagers.forEach(pwaManager -> {

      var submittedEmailProps = new ApplicationSubmittedEmailProps(
          pwaManager.getFullName(),
          detail.getPwaApplicationRef(),
          detail.getPwaApplicationType().getDisplayName(),
          caseLinkService.generateCaseManagementLink(detail.getPwaApplication()));

      notifyService.sendEmail(submittedEmailProps, pwaManager.email());

    });

  }

}
