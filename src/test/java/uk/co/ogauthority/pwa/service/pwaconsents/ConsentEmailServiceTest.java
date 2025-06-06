package uk.co.ogauthority.pwa.service.pwaconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.Assignment;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.AssignmentService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignment;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.CaseOfficerConsentIssuedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ConsentIssuedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ConsentReviewReturnedEmailProps;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ConsentEmailServiceTest {

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private PersonService personService;

  @Mock
  private AssignmentService assignmentService;

  @Mock
  private EmailService emailService;

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


  @BeforeEach
  void setUp() {

    consentEmailService = new ConsentEmailService(caseLinkService, personService, assignmentService, emailService);

  }

  @Test
  void sendConsentReviewReturnedEmail() {

    when(caseLinkService.generateCaseManagementLink(any())).thenCallRealMethod();

    consentEmailService.sendConsentReviewReturnedEmail(pwaApplicationDetail, caseOfficerPerson.getEmailAddress(),
        caseOfficerPerson.getFullName(), returningUser.getLinkedPerson().getFullName(), "return reason");
    verify(emailService).sendEmail(
        consentReviewReturnedEmailCaptor.capture(),
        refEq(EmailRecipient.directEmailAddress(caseOfficerPerson.getEmailAddress())),
        eq(pwaApplicationDetail.getPwaApplicationRef())
    );

    assertThat(consentReviewReturnedEmailCaptor.getValue().getEmailPersonalisation()).containsAllEntriesOf(Map.of(
        "RECIPIENT_FULL_NAME", caseOfficerPerson.getFullName(),
        "RETURNING_PERSON_NAME", returningUser.getLinkedPerson().getFullName(),
        "APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef(),
        "RETURN_REASON", "return reason",
        "CASE_MANAGEMENT_LINK", caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication())
    ));
  }

  @Test
  void sendCaseOfficerConsentIssuedEmail() {
    when(personService.getPersonById(caseOfficerPerson.getId())).thenReturn(caseOfficerPerson);
    when(assignmentService.getAssignmentsForWorkflowAssignment(pwaApplicationDetail.getPwaApplication(), WorkflowAssignment.CASE_OFFICER))
        .thenReturn(Optional.of(assignment));
    consentEmailService.sendCaseOfficerConsentIssuedEmail(pwaApplicationDetail, "PWA Admin");
    verify(emailService).sendEmail(caseOfficerConsentIssuedEmailCaptor.capture(),
        eq(caseOfficerPerson), eq(pwaApplicationDetail.getPwaApplicationRef()));

    assertThat(caseOfficerConsentIssuedEmailCaptor.getValue().getEmailPersonalisation()).containsAllEntriesOf(Map.of(
        "RECIPIENT_FULL_NAME", caseOfficerPerson.getFullName(),
        "APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef(),
        "ISSUING_PERSON_NAME", "PWA Admin"
    ));
  }

  @Test
  void sendHolderAndSubmitterConsentIssuedEmail() {

    when(caseLinkService.generateCaseManagementLink(any())).thenCallRealMethod();

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
        verify(emailService, atLeastOnce()).sendEmail(
            consentIssuedEmailPropsCaptor.capture(),
            eq(recipientPerson),
            eq(pwaApplicationDetail.getPwaApplicationRef())
        );

        var caseManagementLink = caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication());

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
  void sendNonHolderConsentIssuedEmail() {

    when(caseLinkService.generateCaseManagementLink(any())).thenCallRealMethod();

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

      var caseManagementLink = caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication());

      emailRecipientPersons.forEach(recipientPerson -> {

        verify(emailService, atLeastOnce()).sendEmail(
            consentIssuedEmailPropsCaptor.capture(),
            eq(recipientPerson),
            eq(pwaApplicationDetail.getPwaApplicationRef())
        );

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
