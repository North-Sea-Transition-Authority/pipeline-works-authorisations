package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ApplicationSubmittedEmailProps;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDataCleanupService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

/**
 * Service to perform all submission business logic for pwa applications.
 */
@Service
public class PwaApplicationSubmissionService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final NotifyService notifyService;
  private final TeamService teamService;
  private final PwaApplicationDataCleanupService pwaApplicationDataCleanupService;

  @Autowired
  public PwaApplicationSubmissionService(PwaApplicationDetailService pwaApplicationDetailService,
                                         CamundaWorkflowService camundaWorkflowService,
                                         NotifyService notifyService,
                                         TeamService teamService,
                                         PwaApplicationDataCleanupService pwaApplicationDataCleanupService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.notifyService = notifyService;
    this.teamService = teamService;
    this.pwaApplicationDataCleanupService = pwaApplicationDataCleanupService;
  }

  @Transactional
  public void submitApplication(WebUserAccount submittedByUser, PwaApplicationDetail detail) {

    if (!detail.isTipFlag()) {
      throw new IllegalArgumentException(String.format("Application Detail not tip! id: %s", detail.getId()));
    }

    if (!detail.getStatus().equals(PwaApplicationStatus.DRAFT)) {
      throw new IllegalArgumentException(
          String.format("Application Detail not draft! id: %s status: %s", detail.getId(), detail.getStatus()));
    }

    pwaApplicationDataCleanupService.cleanupData(detail);

    camundaWorkflowService.completeTask(
        new WorkflowTaskInstance(detail.getPwaApplication(), PwaApplicationWorkflowTask.PREPARE_APPLICATION));

    pwaApplicationDetailService.setSubmitted(detail, submittedByUser);

    sendApplicationSubmittedEmail(detail);

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
