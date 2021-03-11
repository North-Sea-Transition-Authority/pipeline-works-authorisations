package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.notify.emailproperties.assignments.CaseOfficerAssignmentFailEmailProps;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;

@Service
class AppChargeEmailService {

  private final PwaTeamService pwaTeamService;
  private final NotifyService notifyService;
  private final EmailCaseLinkService emailCaseLinkService;

  @Autowired
  AppChargeEmailService(PwaTeamService pwaTeamService,
                        NotifyService notifyService,
                        EmailCaseLinkService emailCaseLinkService) {
    this.pwaTeamService = pwaTeamService;
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
  }


  public void sendFailedToAssignCaseOfficerEmail(PwaApplication pwaApplication) {

    var pwaManagerPeople = pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER);
    var caseLink = emailCaseLinkService.generateCaseManagementLink(pwaApplication);

    for (Person pwaManager : pwaManagerPeople) {
      var emailProps = new CaseOfficerAssignmentFailEmailProps(
          pwaManager.getFullName(),
          pwaApplication.getAppReference(),
          caseLink
      );

      notifyService.sendEmail(emailProps, pwaManager.getEmailAddress());
    }

  }

}
