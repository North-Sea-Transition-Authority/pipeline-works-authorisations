package uk.co.ogauthority.pwa.service.pwaconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.CaseOfficerConsentIssuedEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.ConsentIssuedEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.ConsentReviewReturnedEmailProps;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.workflow.assignment.AssignmentService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsentEmailServiceTest {

  @Mock
  private NotifyService notifyService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Mock
  private PersonService personService;

  @Mock
  private AssignmentService assignmentService;

  @Captor
  private ArgumentCaptor<ConsentReviewReturnedEmailProps> consentReviewReturnedEmailCaptor;

  @Captor
  private ArgumentCaptor<CaseOfficerConsentIssuedEmailProps> caseOfficerConsentIssuedEmailCaptor;

  @Captor
  private ArgumentCaptor<ConsentIssuedEmailProps> consentIssuedEmailPropsCaptor;

  private ConsentEmailService consentEmailService;

  private final PwaApplicationDetail pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  private final Person caseOfficerPerson = PersonTestUtil.createDefaultPerson();
  private final AuthenticatedUserAccount returningUser =
      new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createPersonFrom(new PersonId(100))), Set.of());
  private final Assignment assignment = new Assignment(pwaApplicationDetail.getId(), WorkflowType.PWA_APPLICATION,
      WorkflowAssignment.CASE_OFFICER, caseOfficerPerson.getId());
  private final String consentReference = "1/W/90";


  @Before
  public void setUp() throws Exception {

    consentEmailService = new ConsentEmailService(notifyService, emailCaseLinkService, personService, assignmentService);

    when(emailCaseLinkService.generateCaseManagementLink(any())).thenCallRealMethod();

  }

  @Test
  public void sendConsentReviewReturnedEmail() {
    consentEmailService.sendConsentReviewReturnedEmail(pwaApplicationDetail, caseOfficerPerson.getEmailAddress(),
        caseOfficerPerson.getFullName(), returningUser.getLinkedPerson().getFullName(), "return reason");
    verify(notifyService).sendEmail(consentReviewReturnedEmailCaptor.capture(),
        eq(caseOfficerPerson.getEmailAddress()));

    assertThat(consentReviewReturnedEmailCaptor.getValue().getEmailPersonalisation()).containsAllEntriesOf(Map.of(
        "RECIPIENT_FULL_NAME", caseOfficerPerson.getFullName(),
        "RETURNING_PERSON_NAME", returningUser.getLinkedPerson().getFullName(),
        "APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef(),
        "RETURN_REASON", "return reason",
        "CASE_MANAGEMENT_LINK", emailCaseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication())
    ));
  }

  @Test
  public void sendCaseOfficerConsentIssuedEmail() {
    when(personService.getPersonById(caseOfficerPerson.getId())).thenReturn(caseOfficerPerson);
    when(assignmentService.getAssignmentsForWorkflowAssignment(pwaApplicationDetail.getPwaApplication(), WorkflowAssignment.CASE_OFFICER))
        .thenReturn(Optional.of(assignment));
    consentEmailService.sendCaseOfficerConsentIssuedEmail(pwaApplicationDetail, "PWA Admin");
    verify(notifyService).sendEmail(caseOfficerConsentIssuedEmailCaptor.capture(),
        eq(caseOfficerPerson.getEmailAddress()));

    assertThat(caseOfficerConsentIssuedEmailCaptor.getValue().getEmailPersonalisation()).containsAllEntriesOf(Map.of(
        "RECIPIENT_FULL_NAME", caseOfficerPerson.getFullName(),
        "APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef(),
        "ISSUING_PERSON_NAME", "PWA Admin"
    ));
  }

  @Test
  public void sendHolderAndSubmitterConsentIssuedEmail() {

    PwaApplicationType.stream().forEach(pwaApplicationType -> {

      pwaApplicationDetail.getPwaApplication().setApplicationType(pwaApplicationType);

      var emailRecipientPersons = List.of(
          PersonTestUtil.createPersonWithNameFrom(new PersonId(100)), PersonTestUtil.createPersonWithNameFrom(new PersonId(200)));
      var coverLetterText = "cover letter text";

      consentEmailService.sendHolderAndSubmitterConsentIssuedEmail(
          pwaApplicationDetail,
          consentReference,
          coverLetterText,
          caseOfficerPerson.getEmailAddress(),
          emailRecipientPersons);

      emailRecipientPersons.forEach(recipientPerson -> {
        verify(notifyService, atLeastOnce()).sendEmail(consentIssuedEmailPropsCaptor.capture(),
            eq(recipientPerson.getEmailAddress()));

        var caseManagementLink = emailCaseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication());

        assertThat(consentIssuedEmailPropsCaptor.getValue().getTemplate()).isEqualTo(pwaApplicationType.getConsentIssueEmail().getHolderEmailTemplate());

        assertThat(consentIssuedEmailPropsCaptor.getValue().getEmailPersonalisation()).containsAllEntriesOf(Map.of(
            "RECIPIENT_FULL_NAME", recipientPerson.getFullName(),
            "APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef(),
            "CONSENT_REFERENCE", consentReference,
            "COVER_LETTER_TEXT", coverLetterText,
            "CASE_OFFICER_EMAIL", caseOfficerPerson.getEmailAddress(),
            "CASE_MANAGEMENT_LINK", caseManagementLink
        ));
      });

    });

  }

  @Test
  public void sendNonHolderConsentIssuedEmail() {

    PwaApplicationType.stream().forEach(pwaApplicationType -> {

      pwaApplicationDetail.getPwaApplication().setApplicationType(pwaApplicationType);

      var emailRecipientPersons = List.of(
          PersonTestUtil.createPersonWithNameFrom(new PersonId(100)), PersonTestUtil.createPersonWithNameFrom(new PersonId(200)));
      var coverLetterText = "cover letter text";

      consentEmailService.sendNonHolderConsentIssuedEmail(
          pwaApplicationDetail,
          consentReference,
          coverLetterText,
          caseOfficerPerson.getEmailAddress(),
          emailRecipientPersons
      );

      var caseManagementLink = emailCaseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication());

      emailRecipientPersons.forEach(recipientPerson -> {

        verify(notifyService, atLeastOnce()).sendEmail(consentIssuedEmailPropsCaptor.capture(),
            eq(recipientPerson.getEmailAddress()));

        assertThat(consentIssuedEmailPropsCaptor.getValue().getTemplate()).isEqualTo(pwaApplicationType.getConsentIssueEmail().getNonHolderEmailTemplate());

        assertThat(consentIssuedEmailPropsCaptor.getValue().getEmailPersonalisation()).containsAllEntriesOf(Map.of(
            "RECIPIENT_FULL_NAME", recipientPerson.getFullName(),
            "APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef(),
            "CONSENT_REFERENCE", consentReference,
            "COVER_LETTER_TEXT", coverLetterText,
            "CASE_OFFICER_EMAIL", caseOfficerPerson.getEmailAddress(),
            "CASE_MANAGEMENT_LINK", caseManagementLink
        ));

      });

    });

  }


}
