package uk.co.ogauthority.pwa.service.pwaapplications.workflow;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.email.EmailCaseLinkService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationSubmitResult;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.submission.PadPipelineNumberingService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationFirstDraftSubmissionServiceTest {

  private static final PersonId PERSON_ID = new PersonId(10);
  private static final String SUBMISSION_DESC = "desc";

  @Mock
  private NotifyService notifyService;

  @Mock
  private PwaTeamService pwaTeamService;

  @Mock
  private PadPipelineNumberingService padPipelineNumberingService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Captor
  private ArgumentCaptor<EmailProperties> emailPropsCaptor;

  private PwaApplicationDetail pwaApplicationDetail;

  private PwaApplicationFirstDraftSubmissionService pwaApplicationFirstDraftSubmissionService;

  private Person person;

  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    pwaApplicationFirstDraftSubmissionService = new PwaApplicationFirstDraftSubmissionService(
        notifyService,
        pwaTeamService,
        padPipelineNumberingService,
        emailCaseLinkService);

    person = new Person(PERSON_ID.asInt(), "first", "second", "email", "tel");
  }

  @Test
  public void getSubmissionWorkflowResult_returnsAsExpected() {
    assertThat(pwaApplicationFirstDraftSubmissionService.getSubmissionWorkflowResult())
        .contains(PwaApplicationSubmitResult.SUBMIT_PREPARED_APPLICATION);
  }

  @Test
  public void getTaskToComplete_returnsExpectedTask() {
    assertThat(pwaApplicationFirstDraftSubmissionService.getTaskToComplete())
        .isEqualTo(PwaApplicationWorkflowTask.PREPARE_APPLICATION);
  }

  @Test
  public void doBeforeSubmit_verifyServiceInteractions() {
    pwaApplicationFirstDraftSubmissionService.doBeforeSubmit(pwaApplicationDetail, person, SUBMISSION_DESC);
      verify(padPipelineNumberingService, times(1)).assignPipelineReferences(pwaApplicationDetail);
      verifyNoMoreInteractions(padPipelineNumberingService, notifyService, pwaTeamService);
  }

  @Test
  public void doAfterSubmit_pwaManagersSentEmail() {

    var pwaManager1 = new Person(1, "PWA", "Manager1", "manager1@pwa.co.uk", null);
    var pwaManager2 = new Person(2, "PWA", "Manager2", "manager2@pwa.co.uk", null);

    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER)).thenReturn(Set.of(pwaManager1, pwaManager2));

    String caseManagementLink = "case management link url";
    when(emailCaseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication())).thenReturn(caseManagementLink);

    pwaApplicationFirstDraftSubmissionService.doAfterSubmit(pwaApplicationDetail);

    verify(notifyService, times(1)).sendEmail(emailPropsCaptor.capture(), eq("manager1@pwa.co.uk"));
    assertThat(emailPropsCaptor.getValue().getEmailPersonalisation().get("RECIPIENT_FULL_NAME")).isEqualTo(
        "PWA Manager1");

    verify(notifyService, times(1)).sendEmail(emailPropsCaptor.capture(), eq("manager2@pwa.co.uk"));
    assertThat(emailPropsCaptor.getValue().getEmailPersonalisation().get("RECIPIENT_FULL_NAME")).isEqualTo(
        "PWA Manager2");

    emailPropsCaptor.getAllValues().forEach(emailProps -> {

      assertThat(emailProps.getTemplate()).isEqualTo(NotifyTemplate.APPLICATION_SUBMITTED);
      assertThat(emailProps.getEmailPersonalisation()).contains(
          entry("APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef()),
          entry("APPLICATION_TYPE", pwaApplicationDetail.getPwaApplicationType().getDisplayName()),
          entry("CASE_MANAGEMENT_LINK", caseManagementLink)
      );

    });

  }

}