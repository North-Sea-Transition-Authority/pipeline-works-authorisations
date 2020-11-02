package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ApplicationSubmittedEmailProps;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationSubmitResult;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.submission.PadPipelineNumberingService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

/**
 * Service to perform all submission business logic for pwa applications.
 */
@Service
class PwaApplicationFirstDraftSubmissionService implements ApplicationSubmissionService {

  private final NotifyService notifyService;
  private final TeamService teamService;
  private final PadPipelineNumberingService padPipelineNumberingService;


  @Autowired
  public PwaApplicationFirstDraftSubmissionService(NotifyService notifyService,
                                                   TeamService teamService,
                                                   PadPipelineNumberingService padPipelineNumberingService) {

    this.notifyService = notifyService;
    this.teamService = teamService;

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

    var pwaManagers = teamService.getTeamMembers(teamService.getRegulatorTeam()).stream()
        .filter(member -> member.getRoleSet().stream()
            .map(PwaRole::getName)
            .anyMatch(roleName -> roleName.equals(PwaRegulatorRole.PWA_MANAGER.getPortalTeamRoleName())))
        .map(PwaTeamMember::getPerson)
        .collect(Collectors.toSet());

    pwaManagers.forEach(pwaManager -> {

      var submittedEmailProps = new ApplicationSubmittedEmailProps(
          pwaManager.getFullName(),
          detail.getPwaApplicationRef(),
          detail.getPwaApplicationType().getDisplayName());

      notifyService.sendEmail(submittedEmailProps, pwaManager.getEmailAddress());

    });

  }

}
