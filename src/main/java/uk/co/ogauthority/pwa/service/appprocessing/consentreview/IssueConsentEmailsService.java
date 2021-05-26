package uk.co.ogauthority.pwa.service.appprocessing.consentreview;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaconsents.ConsentEmailService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;

@Service
public class IssueConsentEmailsService {

  private final ConsentEmailService consentEmailService;
  private final PwaContactService pwaContactService;
  private final PwaHolderTeamService pwaHolderTeamService;
  private final PersonService personService;

  @Autowired
  public IssueConsentEmailsService(ConsentEmailService consentEmailService,
                                   PwaContactService pwaContactService,
                                   PwaHolderTeamService pwaHolderTeamService,
                                   PersonService personService) {
    this.consentEmailService = consentEmailService;
    this.pwaContactService = pwaContactService;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.personService = personService;
  }



  public void sendConsentIssuedEmails(PwaApplicationDetail pwaApplicationDetail, String coverLetterText, String issuingUserName) {

    consentEmailService.sendConsentIssuedEmail(pwaApplicationDetail, issuingUserName);

    var holderContactsAndAppSubmitterRecipients = new ArrayList<Person>();
    var nonHolderContactsRecipients = new ArrayList<Person>();
    setHolderAndNonHolderRecipients(holderContactsAndAppSubmitterRecipients, nonHolderContactsRecipients, pwaApplicationDetail);
    addLatestAppSubmitterToRecipientsIfDoesntExist(pwaApplicationDetail, holderContactsAndAppSubmitterRecipients);

    consentEmailService.sendHolderAndSubmitterConsentIssuedEmail(
        pwaApplicationDetail, coverLetterText, holderContactsAndAppSubmitterRecipients);
    consentEmailService.sendNonHolderConsentIssuedEmail(pwaApplicationDetail, coverLetterText, nonHolderContactsRecipients);
  }

  private void setHolderAndNonHolderRecipients(ArrayList<Person> holderContactsAndAppSubmitterRecipients,
                                               ArrayList<Person> nonHolderContactsRecipients,
                                               PwaApplicationDetail pwaApplicationDetail) {

    pwaContactService.getContactsForPwaApplication(pwaApplicationDetail.getPwaApplication())
        .forEach(appContact -> {
          var holderTeamRoles = pwaHolderTeamService.getRolesInHolderTeam(pwaApplicationDetail, appContact.getPerson());
          if (holderTeamRoles.isEmpty()) {
            nonHolderContactsRecipients.add(appContact.getPerson());

          } else {
            holderContactsAndAppSubmitterRecipients.add(appContact.getPerson());
          }
        });
  }


  private void addLatestAppSubmitterToRecipientsIfDoesntExist(PwaApplicationDetail pwaApplicationDetail, List<Person> recipients) {

    if (!pwaApplicationDetail.isTipFlag()) {
      throw new ActionNotAllowedException(
          "Cannot send the consent issued email to latest application submitter as the PwaApplicationDetail provided " +
              "is not the latest version for  id: " + pwaApplicationDetail.getId());
    }

    var latestAppSubmitter = personService.getPersonById(pwaApplicationDetail.getSubmittedByPersonId());
    if (!recipients.contains(latestAppSubmitter)) {
      recipients.add(latestAppSubmitter);
    }
  }



}
