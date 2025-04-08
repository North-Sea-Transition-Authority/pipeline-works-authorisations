package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ConsentReviewEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class ConsentDocumentEmailService {

  private final CaseLinkService caseLinkService;
  private final TeamQueryService teamQueryService;
  private final EmailService emailService;

  @Autowired
  public ConsentDocumentEmailService(CaseLinkService caseLinkService,
                                     TeamQueryService teamQueryService,
                                     EmailService emailService) {
    this.caseLinkService = caseLinkService;
    this.teamQueryService = teamQueryService;
    this.emailService = emailService;
  }

  public void sendConsentReviewStartedEmail(PwaApplicationDetail pwaApplicationDetail,
                                            Person sendingPerson) {

    var pwaManagers = teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, Role.PWA_MANAGER);

    String caseManagementLink = caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication());

    pwaManagers.forEach(pwaManager -> {

      var consentReviewEmailProps = new ConsentReviewEmailProps(
          pwaManager.getFullName(),
          pwaApplicationDetail.getPwaApplicationRef(),
          sendingPerson.getFullName(),
          caseManagementLink);

      emailService.sendEmail(
          consentReviewEmailProps,
          EmailRecipient.directEmailAddress(pwaManager.email()),
          pwaApplicationDetail.getPwaApplicationRef()
      );
    });

  }

}
