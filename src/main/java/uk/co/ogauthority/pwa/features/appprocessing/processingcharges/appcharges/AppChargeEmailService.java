package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContact;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationpayments.ApplicationPaymentRequestCancelledEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationpayments.ApplicationPaymentRequestIssuedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.assignments.CaseOfficerAssignmentFailEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
class AppChargeEmailService {

  private final PwaContactService pwaContactService;
  private final CaseLinkService caseLinkService;
  private final TeamQueryService teamQueryService;
  private final EmailService emailService;

  @Autowired
  AppChargeEmailService(PwaContactService pwaContactService,
                        CaseLinkService caseLinkService,
                        TeamQueryService teamQueryService,
                        EmailService emailService) {
    this.pwaContactService = pwaContactService;
    this.caseLinkService = caseLinkService;
    this.teamQueryService = teamQueryService;
    this.emailService = emailService;
  }

  public void sendFailedToAssignCaseOfficerEmail(PwaApplication pwaApplication) {

    var pwaManagerPeople = teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, Role.PWA_MANAGER);
    var caseLink = caseLinkService.generateCaseManagementLink(pwaApplication);

    for (var pwaManager : pwaManagerPeople) {
      var emailProps = new CaseOfficerAssignmentFailEmailProps(
          pwaManager.getFullName(),
          pwaApplication.getAppReference(),
          caseLink
      );

      emailService.sendEmail(emailProps, EmailRecipient.directEmailAddress(pwaManager.email()), pwaApplication.getAppReference());
    }

  }

  public void sendChargeRequestIssuedEmail(PwaApplication pwaApplication) {
    var appContactPeople = pwaContactService.getContactsForPwaApplication(pwaApplication)
        .stream()
        .map(PwaContact::getPerson)
        .toList();
    var caseLink = caseLinkService.generateCaseManagementLink(pwaApplication);

    for (Person appContactPerson : appContactPeople) {
      var emailProps = new ApplicationPaymentRequestIssuedEmailProps(
          appContactPerson.getFullName(),
          pwaApplication.getAppReference(),
          caseLink
      );

      emailService.sendEmail(emailProps, appContactPerson, pwaApplication.getAppReference());
    }

  }

  public void sendChargeRequestCancelledEmail(PwaApplication pwaApplication) {
    var appContactPeople = pwaContactService.getContactsForPwaApplication(pwaApplication)
        .stream()
        .map(PwaContact::getPerson)
        .toList();

    for (Person appContactPerson : appContactPeople) {
      var emailProps = new ApplicationPaymentRequestCancelledEmailProps(
          appContactPerson.getFullName(),
          pwaApplication.getAppReference()
      );

      emailService.sendEmail(emailProps, appContactPerson, pwaApplication.getAppReference());
    }
  }

}
