package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ConsentReviewEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;

@Service
public class ConsentDocumentEmailService {

  private final NotifyService notifyService;
  private final CaseLinkService caseLinkService;
  private final PwaTeamService pwaTeamService;

  @Autowired
  public ConsentDocumentEmailService(NotifyService notifyService,
                                     CaseLinkService caseLinkService,
                                     PwaTeamService pwaTeamService) {
    this.notifyService = notifyService;
    this.caseLinkService = caseLinkService;
    this.pwaTeamService = pwaTeamService;
  }

  public void sendConsentReviewStartedEmail(PwaApplicationDetail pwaApplicationDetail,
                                            Person sendingPerson) {

    var pwaManagers = pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER);

    String caseManagementLink = caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication());

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
