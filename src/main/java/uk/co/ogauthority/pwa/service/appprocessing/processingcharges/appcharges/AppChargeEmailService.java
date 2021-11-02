package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

import static java.util.stream.Collectors.toList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContact;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationpayments.ApplicationPaymentRequestCancelledEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationpayments.ApplicationPaymentRequestIssuedEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.assignments.CaseOfficerAssignmentFailEmailProps;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;

@Service
class AppChargeEmailService {

  private final PwaTeamService pwaTeamService;
  private final PwaContactService pwaContactService;
  private final NotifyService notifyService;
  private final EmailCaseLinkService emailCaseLinkService;

  @Autowired
  AppChargeEmailService(PwaTeamService pwaTeamService,
                        PwaContactService pwaContactService,
                        NotifyService notifyService,
                        EmailCaseLinkService emailCaseLinkService) {
    this.pwaTeamService = pwaTeamService;
    this.pwaContactService = pwaContactService;
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

  public void sendChargeRequestIssuedEmail(PwaApplication pwaApplication) {
    var appContactPeople = pwaContactService.getContactsForPwaApplication(pwaApplication)
        .stream()
        .map(PwaContact::getPerson)
        .collect(toList());
    var caseLink = emailCaseLinkService.generateCaseManagementLink(pwaApplication);

    for (Person appContactPerson : appContactPeople) {
      var emailProps = new ApplicationPaymentRequestIssuedEmailProps(
          appContactPerson.getFullName(),
          pwaApplication.getAppReference(),
          caseLink
      );

      notifyService.sendEmail(emailProps, appContactPerson.getEmailAddress());
    }

  }

  public void sendChargeRequestCancelledEmail(PwaApplication pwaApplication) {
    var appContactPeople = pwaContactService.getContactsForPwaApplication(pwaApplication)
        .stream()
        .map(PwaContact::getPerson)
        .collect(toList());

    for (Person appContactPerson : appContactPeople) {
      var emailProps = new ApplicationPaymentRequestCancelledEmailProps(
          appContactPerson.getFullName(),
          pwaApplication.getAppReference()
      );

      notifyService.sendEmail(emailProps, appContactPerson.getEmailAddress());
    }
  }

}
