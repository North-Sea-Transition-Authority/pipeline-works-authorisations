package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ConsentReviewEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class ConsentDocumentEmailService {

  private final NotifyService notifyService;
  private final CaseLinkService caseLinkService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public ConsentDocumentEmailService(NotifyService notifyService,
                                     CaseLinkService caseLinkService,
                                     TeamQueryService teamQueryService) {
    this.notifyService = notifyService;
    this.caseLinkService = caseLinkService;
    this.teamQueryService = teamQueryService;
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

      notifyService.sendEmail(consentReviewEmailProps, pwaManager.email());

    });

  }

}
