package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContact;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactTestUtil;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationpayments.ApplicationPaymentRequestCancelledEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationpayments.ApplicationPaymentRequestIssuedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.assignments.CaseOfficerAssignmentFailEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppChargeEmailServiceTest {
  private static final String CASE_LINK = "LINK";

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private EmailService emailService;

  @Captor
  private ArgumentCaptor<CaseOfficerAssignmentFailEmailProps> assignmentFailEmailPropsCaptor;

  @Captor
  private ArgumentCaptor<ApplicationPaymentRequestIssuedEmailProps> requestIssuedEmailPropsCaptor;

  @Captor
  private ArgumentCaptor<ApplicationPaymentRequestCancelledEmailProps> requestCancelledEmailPropsCaptor;

  @InjectMocks
  private AppChargeEmailService appChargeEmailService;

  private Person  appContact1Person, appContact2Person;

  private PwaContact contact1, contact2;

  private PwaApplication pwaApplication;
  private TeamMemberView pwaManager1;
  private TeamMemberView pwaManager2;

  @BeforeEach
  void setUp() {
    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL)
        .getPwaApplication();

    pwaManager1 = new TeamMemberView(1L, "Mr.", "PWA", "Manager1", "manager1@pwa.co.uk", null, null, null);
    pwaManager2 = new TeamMemberView(2L, "Ms.", "PWA", "Manager2", "manager2@pwa.co.uk", null, null, null);

    when(teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, Role.PWA_MANAGER)).thenReturn(List.of(pwaManager1, pwaManager2));

    appContact1Person = PersonTestUtil.createPersonFrom(new PersonId(3), "contact1@email", "contact1");
    appContact2Person = PersonTestUtil.createPersonFrom(new PersonId(4), "contact2@email", "contact2");

    contact1 = PwaContactTestUtil.createBasicAllRoleContact(appContact1Person);
    contact2 = PwaContactTestUtil.createBasicAllRoleContact(appContact2Person);
    when(pwaContactService.getContactsForPwaApplication(pwaApplication))
        .thenReturn(List.of(contact1, contact2));

    when(caseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(CASE_LINK);

  }

  @Test
  void sendFailedToAssignCaseOfficerEmail_emailsPwaManagers() {

    when(teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, Role.PWA_MANAGER)).thenReturn(List.of(pwaManager1, pwaManager2));

    appChargeEmailService.sendFailedToAssignCaseOfficerEmail(pwaApplication);

    verify(emailService, times(2)).sendEmail(assignmentFailEmailPropsCaptor.capture(), refEq(EmailRecipient.directEmailAddress(pwaManager1.email())),
        eq(pwaApplication.getAppReference()));

    assertThat(assignmentFailEmailPropsCaptor.getAllValues())
        .hasSize(2)
        .allSatisfy(caseOfficerAssignmentFailEmailProps -> {
          assertThat(caseOfficerAssignmentFailEmailProps.getCaseManagementLink()).isEqualTo(CASE_LINK);
          assertThat(caseOfficerAssignmentFailEmailProps.getApplicationReference()).isEqualTo(
              pwaApplication.getAppReference());
        })
        .anySatisfy(caseOfficerAssignmentFailEmailProps ->
          assertThat(caseOfficerAssignmentFailEmailProps.getRecipientFullName()).isEqualTo(pwaManager1.getFullName()))
        .anySatisfy(caseOfficerAssignmentFailEmailProps ->
          assertThat(caseOfficerAssignmentFailEmailProps.getRecipientFullName()).isEqualTo(pwaManager2.getFullName()));

  }

  @Test
  void sendChargeRequestIssuedEmail_emailsAppContacts() {

    appChargeEmailService.sendChargeRequestIssuedEmail(pwaApplication);

    verify(emailService).sendEmail(requestIssuedEmailPropsCaptor.capture(), eq(appContact1Person), eq(pwaApplication.getAppReference()));
    verify(emailService).sendEmail(requestIssuedEmailPropsCaptor.capture(), eq(appContact2Person), eq(pwaApplication.getAppReference()));

    assertThat(requestIssuedEmailPropsCaptor.getAllValues())
        .hasSize(2)
        .allSatisfy(chargeRequestIssuedEmailProps -> {
          assertThat(chargeRequestIssuedEmailProps.getCaseManagementLink()).isEqualTo(CASE_LINK);
          assertThat(chargeRequestIssuedEmailProps.getApplicationReference()).isEqualTo(
              pwaApplication.getAppReference());
        })
        .anySatisfy(caseOfficerAssignmentFailEmailProps ->
          assertThat(caseOfficerAssignmentFailEmailProps.getRecipientFullName()).isEqualTo(appContact1Person.getFullName()))
        .anySatisfy(caseOfficerAssignmentFailEmailProps ->
          assertThat(caseOfficerAssignmentFailEmailProps.getRecipientFullName()).isEqualTo(appContact2Person.getFullName()));
  }

  @Test
  void sendChargeRequestCancelledEmail_emailsAppContacts() {
    appChargeEmailService.sendChargeRequestCancelledEmail(pwaApplication);

    verify(emailService).sendEmail(requestCancelledEmailPropsCaptor.capture(), eq(appContact1Person), eq(pwaApplication.getAppReference()));
    verify(emailService).sendEmail(requestCancelledEmailPropsCaptor.capture(), eq(appContact2Person), eq(pwaApplication.getAppReference()));

    assertThat(requestCancelledEmailPropsCaptor.getAllValues())
        .hasSize(2)
        .allSatisfy(chargeRequestCancelledEmailProps ->
          assertThat(chargeRequestCancelledEmailProps.getApplicationReference()).isEqualTo(
              pwaApplication.getAppReference()))
        .anySatisfy(caseOfficerAssignmentFailEmailProps ->
          assertThat(caseOfficerAssignmentFailEmailProps.getRecipientFullName()).isEqualTo(appContact1Person.getFullName()))
        .anySatisfy(caseOfficerAssignmentFailEmailProps ->
          assertThat(caseOfficerAssignmentFailEmailProps.getRecipientFullName()).isEqualTo(appContact2Person.getFullName()));

  }
}