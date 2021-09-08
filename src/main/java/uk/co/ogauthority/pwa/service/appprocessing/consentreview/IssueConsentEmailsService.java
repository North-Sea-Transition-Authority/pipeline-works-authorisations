package uk.co.ogauthority.pwa.service.appprocessing.consentreview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

  public void sendConsentIssuedEmails(PwaApplicationDetail pwaApplicationDetail,
                                      String consentReference,
                                      String coverLetterText,
                                      String caseOfficerEmail,
                                      String issuingUserName) {

    consentEmailService.sendCaseOfficerConsentIssuedEmail(pwaApplicationDetail, issuingUserName);

    var holderContactsAndAppSubmitterRecipients = new HashSet<Person>();
    var nonHolderContactsRecipients = new ArrayList<Person>();
    setHolderAndNonHolderRecipients(holderContactsAndAppSubmitterRecipients, nonHolderContactsRecipients, pwaApplicationDetail);
    addLatestAppSubmitterToRecipientsIfDoesntExist(pwaApplicationDetail, holderContactsAndAppSubmitterRecipients);

    consentEmailService.sendHolderAndSubmitterConsentIssuedEmail(
        pwaApplicationDetail,
        consentReference,
        coverLetterText,
        caseOfficerEmail,
        holderContactsAndAppSubmitterRecipients);

    consentEmailService.sendNonHolderConsentIssuedEmail(
        pwaApplicationDetail,
        consentReference,
        coverLetterText,
        caseOfficerEmail,
        nonHolderContactsRecipients);

  }

  private void setHolderAndNonHolderRecipients(Collection<Person> holderContactsAndAppSubmitterRecipients,
                                               List<Person> nonHolderContactsRecipients,
                                               PwaApplicationDetail pwaApplicationDetail) {

    var holderTeamPeople = pwaHolderTeamService.getPersonsInHolderTeam(pwaApplicationDetail);
    pwaContactService.getContactsForPwaApplication(pwaApplicationDetail.getPwaApplication())
        .forEach(appContact -> {
          if (holderTeamPeople.contains(appContact.getPerson())) {
            holderContactsAndAppSubmitterRecipients.add(appContact.getPerson());

          } else {
            nonHolderContactsRecipients.add(appContact.getPerson());
          }
        });
  }


  private void addLatestAppSubmitterToRecipientsIfDoesntExist(PwaApplicationDetail pwaApplicationDetail, Set<Person> recipients) {

    if (!pwaApplicationDetail.isTipFlag()) {
      throw new ActionNotAllowedException(
          "Cannot send the consent issued email to latest application submitter as the PwaApplicationDetail provided " +
              "is not the latest version for  id: " + pwaApplicationDetail.getId());
    }

    var latestAppSubmitter = personService.getPersonById(pwaApplicationDetail.getSubmittedByPersonId());
    recipients.add(latestAppSubmitter);

  }

  public void sendConsentReviewReturnedEmail(PwaApplicationDetail pwaApplicationDetail,
                                             Person caseOfficerPerson,
                                             String returningUserName,
                                             String returnReason) {
    consentEmailService.sendConsentReviewReturnedEmail(pwaApplicationDetail, caseOfficerPerson.getEmailAddress(),
        caseOfficerPerson.getFullName(), returningUserName, returnReason);
  }



}
