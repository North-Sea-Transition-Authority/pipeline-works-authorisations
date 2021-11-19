package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContact;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactTestUtil;
import uk.co.ogauthority.pwa.features.email.EmailCaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationpayments.ApplicationPaymentRequestCancelledEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationpayments.ApplicationPaymentRequestIssuedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.assignments.CaseOfficerAssignmentFailEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class AppChargeEmailServiceTest {
  private static final String CASE_LINK = "LINK";

  @Mock
  private PwaTeamService pwaTeamService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Mock
  private PwaContactService pwaContactService;

  @Captor
  private ArgumentCaptor<CaseOfficerAssignmentFailEmailProps> assignmentFailEmailPropsCaptor;

  @Captor
  private ArgumentCaptor<ApplicationPaymentRequestIssuedEmailProps> requestIssuedEmailPropsCaptor;

  @Captor
  private ArgumentCaptor<ApplicationPaymentRequestCancelledEmailProps> requestCancelledEmailPropsCaptor;


  private AppChargeEmailService appChargeEmailService;

  private Person pwaManager1, pwaManager2, appContact1Person, appContact2Person;

  private PwaContact contact1, contact2;

  private PwaApplication pwaApplication;

  @Before
  public void setUp() throws Exception {
    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL)
        .getPwaApplication();

    pwaManager1 = PersonTestUtil.createPersonFrom(new PersonId(1), "manager1@email", "manager1");
    pwaManager2 = PersonTestUtil.createPersonFrom(new PersonId(2), "manager2@email", "manager2");

    appContact1Person = PersonTestUtil.createPersonFrom(new PersonId(3), "contact1@email", "contact1");
    appContact2Person = PersonTestUtil.createPersonFrom(new PersonId(4), "contact2@email", "contact2");

    contact1 = PwaContactTestUtil.createBasicAllRoleContact(appContact1Person);
    contact2 = PwaContactTestUtil.createBasicAllRoleContact(appContact2Person);
    when(pwaContactService.getContactsForPwaApplication(pwaApplication))
        .thenReturn(List.of(contact1, contact2));

    appChargeEmailService = new AppChargeEmailService(
        pwaTeamService, pwaContactService, notifyService, emailCaseLinkService
    );

    when(emailCaseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(CASE_LINK);

  }

  @Test
  public void sendFailedToAssignCaseOfficerEmail_emailsPwaManagers() {

    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER))
        .thenReturn(Set.of(pwaManager1, pwaManager2));

    appChargeEmailService.sendFailedToAssignCaseOfficerEmail(pwaApplication);

    verify(notifyService, times(1)).sendEmail(assignmentFailEmailPropsCaptor.capture(), eq(pwaManager1.getEmailAddress()));
    verify(notifyService, times(1)).sendEmail(assignmentFailEmailPropsCaptor.capture(), eq(pwaManager2.getEmailAddress()));

    assertThat(assignmentFailEmailPropsCaptor.getAllValues())
        .hasSize(2)
        .allSatisfy(caseOfficerAssignmentFailEmailProps -> {
          assertThat(caseOfficerAssignmentFailEmailProps.getCaseManagementLink()).isEqualTo(CASE_LINK);
          assertThat(caseOfficerAssignmentFailEmailProps.getApplicationReference()).isEqualTo(
              pwaApplication.getAppReference());
        })
        .anySatisfy(caseOfficerAssignmentFailEmailProps -> {
          assertThat(caseOfficerAssignmentFailEmailProps.getRecipientFullName()).isEqualTo(pwaManager1.getFullName());
        })
        .anySatisfy(caseOfficerAssignmentFailEmailProps -> {
          assertThat(caseOfficerAssignmentFailEmailProps.getRecipientFullName()).isEqualTo(pwaManager2.getFullName());
        });

  }

  @Test
  public void sendChargeRequestIssuedEmail_emailsAppContacts() {

    appChargeEmailService.sendChargeRequestIssuedEmail(pwaApplication);

    verify(notifyService, times(1)).sendEmail(requestIssuedEmailPropsCaptor.capture(), eq(appContact1Person.getEmailAddress()));
    verify(notifyService, times(1)).sendEmail(requestIssuedEmailPropsCaptor.capture(), eq(appContact2Person.getEmailAddress()));

    assertThat(requestIssuedEmailPropsCaptor.getAllValues())
        .hasSize(2)
        .allSatisfy(chargeRequestIssuedEmailProps -> {
          assertThat(chargeRequestIssuedEmailProps.getCaseManagementLink()).isEqualTo(CASE_LINK);
          assertThat(chargeRequestIssuedEmailProps.getApplicationReference()).isEqualTo(
              pwaApplication.getAppReference());
        })
        .anySatisfy(caseOfficerAssignmentFailEmailProps -> {
          assertThat(caseOfficerAssignmentFailEmailProps.getRecipientFullName()).isEqualTo(appContact1Person.getFullName());
        })
        .anySatisfy(caseOfficerAssignmentFailEmailProps -> {
          assertThat(caseOfficerAssignmentFailEmailProps.getRecipientFullName()).isEqualTo(appContact2Person.getFullName());
        });
  }

  @Test
  public void sendChargeRequestCancelledEmail_emailsAppContacts() {
    appChargeEmailService.sendChargeRequestCancelledEmail(pwaApplication);

    verify(notifyService, times(1)).sendEmail(requestCancelledEmailPropsCaptor.capture(), eq(appContact1Person.getEmailAddress()));
    verify(notifyService, times(1)).sendEmail(requestCancelledEmailPropsCaptor.capture(), eq(appContact2Person.getEmailAddress()));

    assertThat(requestCancelledEmailPropsCaptor.getAllValues())
        .hasSize(2)
        .allSatisfy(chargeRequestCancelledEmailProps -> {
          assertThat(chargeRequestCancelledEmailProps.getApplicationReference()).isEqualTo(
              pwaApplication.getAppReference());
        })
        .anySatisfy(caseOfficerAssignmentFailEmailProps -> {
          assertThat(caseOfficerAssignmentFailEmailProps.getRecipientFullName()).isEqualTo(appContact1Person.getFullName());
        })
        .anySatisfy(caseOfficerAssignmentFailEmailProps -> {
          assertThat(caseOfficerAssignmentFailEmailProps.getRecipientFullName()).isEqualTo(appContact2Person.getFullName());
        });

  }
}