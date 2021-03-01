package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.ConsentReviewEmailProps;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;

@Service
public class ConsentDocumentEmailService {

  private final NotifyService notifyService;
  private final EmailCaseLinkService emailCaseLinkService;
  private final PwaTeamService pwaTeamService;

  @Autowired
  public ConsentDocumentEmailService(NotifyService notifyService,
                                     EmailCaseLinkService emailCaseLinkService,
                                     PwaTeamService pwaTeamService) {
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.pwaTeamService = pwaTeamService;
  }

  public void sendConsentReviewStartedEmail(PwaApplicationDetail pwaApplicationDetail,
                                            Person sendingPerson) {

    var pwaManagers = pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER);

    String caseManagementLink = emailCaseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication());

    pwaManagers.forEach(pwaManager -> {

      var consentReviewEmailProps = new ConsentReviewEmailProps(
          pwaManager.getFullName(),
          pwaApplicationDetail.getPwaApplicationRef(),
          sendingPerson.getFullName(),
          caseManagementLink);

      notifyService.sendEmail(consentReviewEmailProps, pwaManager.getEmailAddress());

    });

  }

}
