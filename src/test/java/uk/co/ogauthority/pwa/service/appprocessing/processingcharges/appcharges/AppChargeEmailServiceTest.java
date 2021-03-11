package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.notify.emailproperties.assignments.CaseOfficerAssignmentFailEmailProps;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
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

  @Captor
  private ArgumentCaptor<CaseOfficerAssignmentFailEmailProps> emailPropsArgumentCaptor;


  private AppChargeEmailService appChargeEmailService;

  private Person pwaManager1, pwaManager2;

  private PwaApplication pwaApplication;

  @Before
  public void setUp() throws Exception {
    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL).getPwaApplication();

    pwaManager1 = PersonTestUtil.createPersonFrom(new PersonId(1), "email1", "manager1");
    pwaManager2 = PersonTestUtil.createPersonFrom(new PersonId(2), "email2", "manager2");

    appChargeEmailService = new AppChargeEmailService(
        pwaTeamService, notifyService, emailCaseLinkService
    );

  }

  @Test
  public void sendFailedToAssignCaseOfficerEmail_emailsCaseOfficers() {

    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER))
        .thenReturn(Set.of(pwaManager1, pwaManager2));
    when(emailCaseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(CASE_LINK);

    appChargeEmailService.sendFailedToAssignCaseOfficerEmail(pwaApplication);

    verify(notifyService, times(1)).sendEmail(emailPropsArgumentCaptor.capture(), eq(pwaManager1.getEmailAddress()));
    verify(notifyService, times(1)).sendEmail(emailPropsArgumentCaptor.capture(), eq(pwaManager2.getEmailAddress()));

    assertThat(emailPropsArgumentCaptor.getAllValues())
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
}