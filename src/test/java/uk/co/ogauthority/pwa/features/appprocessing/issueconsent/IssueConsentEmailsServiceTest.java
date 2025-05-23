package uk.co.ogauthority.pwa.features.appprocessing.issueconsent;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.PwaConsentReviewTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaconsents.ConsentEmailService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class IssueConsentEmailsServiceTest {

  @Mock
  private ConsentEmailService consentEmailService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  @Mock
  private PersonService personService;

  private IssueConsentEmailsService issueConsentEmailsService;

  private final PwaApplicationDetail pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  private final AuthenticatedUserAccount issuingUser = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createPersonWithNameFrom(new PersonId(100))), Set.of());

  private final String consentReference = "1/W/89";
  private final String caseOfficerEmail = "case@officer.com";

  @BeforeEach
  void setUp() throws Exception {

    issueConsentEmailsService = new IssueConsentEmailsService(consentEmailService, pwaContactService, pwaHolderTeamService, personService);

  }

  @Test
  void sendConsentIssuedEmails_allEmailsSent() {

    var consentReview = PwaConsentReviewTestUtil.createApprovedConsentReview(pwaApplicationDetail);

    var holderTeamContact = PwaContactTestUtil.createBasicAllRoleContact(PersonTestUtil.createPersonFrom(new PersonId(200)));
    var nonHolderTeamContact = PwaContactTestUtil.createBasicAllRoleContact(PersonTestUtil.createPersonFrom(new PersonId(300)));
    when(pwaContactService.getContactsForPwaApplication(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(List.of(holderTeamContact, nonHolderTeamContact));

    when(pwaHolderTeamService.getPersonsInHolderTeam(pwaApplicationDetail))
        .thenReturn(Set.of(holderTeamContact.getPerson()));

    pwaApplicationDetail.setSubmittedByPersonId(holderTeamContact.getPerson().getId());
    when(personService.getPersonById(pwaApplicationDetail.getSubmittedByPersonId())).thenReturn(holderTeamContact.getPerson());

    issueConsentEmailsService.sendConsentIssuedEmails(
        pwaApplicationDetail,
        consentReference,
        consentReview.getCoverLetterText(),
        caseOfficerEmail,
        issuingUser.getFullName());

    verify(consentEmailService).sendCaseOfficerConsentIssuedEmail(pwaApplicationDetail, issuingUser.getFullName());

    verify(consentEmailService).sendHolderAndSubmitterConsentIssuedEmail(
        pwaApplicationDetail,
        consentReference,
        consentReview.getCoverLetterText(),
        caseOfficerEmail,
        Set.of(holderTeamContact.getPerson()));

    verify(consentEmailService).sendNonHolderConsentIssuedEmail(
        pwaApplicationDetail,
        consentReference,
        consentReview.getCoverLetterText(),
        caseOfficerEmail,
        List.of(nonHolderTeamContact.getPerson()));

  }

  @Test
  void sendConsentIssuedEmails_latestAppSubmitterNotInContacts_latestAppSubmitterIsStillSentEmail() {

    var consentReview = PwaConsentReviewTestUtil.createApprovedConsentReview(pwaApplicationDetail);

    var holderTeamContact = PwaContactTestUtil.createBasicAllRoleContact(PersonTestUtil.createPersonFrom(new PersonId(200)));
    when(pwaContactService.getContactsForPwaApplication(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(List.of(holderTeamContact));

    when(pwaHolderTeamService.getPersonsInHolderTeam(pwaApplicationDetail))
        .thenReturn(Set.of(holderTeamContact.getPerson()));

    var appSubmitterPerson = PersonTestUtil.createPersonFrom(new PersonId(400));
    pwaApplicationDetail.setSubmittedByPersonId(appSubmitterPerson.getId());
    when(personService.getPersonById(pwaApplicationDetail.getSubmittedByPersonId())).thenReturn(appSubmitterPerson);

    issueConsentEmailsService.sendConsentIssuedEmails(
        pwaApplicationDetail,
        consentReference,
        consentReview.getCoverLetterText(),
        caseOfficerEmail,
        issuingUser.getFullName());

    verify(consentEmailService).sendHolderAndSubmitterConsentIssuedEmail(
        pwaApplicationDetail,
        consentReference,
        consentReview.getCoverLetterText(),
        caseOfficerEmail,
        Set.of(holderTeamContact.getPerson(),
        appSubmitterPerson));

  }

  @Test
  void sendConsentIssuedEmails_latestAppSubmitterNotInContacts_appDetailIsNotLatest() {
    var consentReview = PwaConsentReviewTestUtil.createApprovedConsentReview(pwaApplicationDetail);
    pwaApplicationDetail.setTipFlag(false);
    assertThrows(ActionNotAllowedException.class, () ->
      issueConsentEmailsService.sendConsentIssuedEmails(
          pwaApplicationDetail,
          consentReference,
          consentReview.getCoverLetterText(),
          caseOfficerEmail,
          issuingUser.getFullName()));

  }

}