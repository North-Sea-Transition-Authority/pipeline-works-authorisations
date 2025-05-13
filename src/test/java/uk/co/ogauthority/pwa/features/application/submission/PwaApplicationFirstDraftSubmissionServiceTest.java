package uk.co.ogauthority.pwa.features.application.submission;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationSubmitResult;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PwaApplicationFirstDraftSubmissionServiceTest {

  private static final PersonId PERSON_ID = new PersonId(10);
  private static final String SUBMISSION_DESC = "desc";

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private PadPipelineNumberingService padPipelineNumberingService;

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private EmailService emailService;

  @Captor
  private ArgumentCaptor<EmailProperties> emailPropsCaptor;

  private PwaApplicationDetail pwaApplicationDetail;


  @InjectMocks
  private PwaApplicationFirstDraftSubmissionService pwaApplicationFirstDraftSubmissionService;

  private Person person;

  @BeforeEach
  void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    person = new Person(PERSON_ID.asInt(), "first", "second", "email", "tel");
  }

  @Test
  void getSubmissionWorkflowResult_returnsAsExpected() {
    assertThat(pwaApplicationFirstDraftSubmissionService.getSubmissionWorkflowResult())
        .contains(PwaApplicationSubmitResult.SUBMIT_PREPARED_APPLICATION);
  }

  @Test
  void getTaskToComplete_returnsExpectedTask() {
    assertThat(pwaApplicationFirstDraftSubmissionService.getTaskToComplete())
        .isEqualTo(PwaApplicationWorkflowTask.PREPARE_APPLICATION);
  }

  @Test
  void doBeforeSubmit_verifyServiceInteractions() {
    pwaApplicationFirstDraftSubmissionService.doBeforeSubmit(pwaApplicationDetail, person, SUBMISSION_DESC);
      verify(padPipelineNumberingService).assignPipelineReferences(pwaApplicationDetail);
      verifyNoMoreInteractions(padPipelineNumberingService, emailService, teamQueryService);
  }

  @Test
  void name() {
  }

  @Test
  void doAfterSubmit_pwaManagersSentEmail() {

    var pwaManager1 = new TeamMemberView(1L, "Mr.", "PWA", "Manager1", "manager1@pwa.co.uk", null, null, null);
    var pwaManager2 = new TeamMemberView(2L, "Ms.", "PWA", "Manager2", "manager2@pwa.co.uk", null, null, null);

    when(teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, Role.PWA_MANAGER)).thenReturn(List.of(pwaManager1, pwaManager2));

    String caseManagementLink = "case management link url";
    when(caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication())).thenReturn(caseManagementLink);

    pwaApplicationFirstDraftSubmissionService.doAfterSubmit(pwaApplicationDetail);

    verify(emailService, times(2)).sendEmail(
        emailPropsCaptor.capture(),
        refEq(EmailRecipient.directEmailAddress("manager1@pwa.co.uk")),
        eq(pwaApplicationDetail.getPwaApplicationRef())
    );
    assertThat(emailPropsCaptor.getAllValues().getFirst().getEmailPersonalisation().get("RECIPIENT_FULL_NAME")).isEqualTo(
        "Mr. PWA Manager1");
    assertThat(emailPropsCaptor.getAllValues().get(1).getEmailPersonalisation().get("RECIPIENT_FULL_NAME")).isEqualTo(
        "Ms. PWA Manager2");

    emailPropsCaptor.getAllValues().forEach(emailProps -> {

      assertThat(emailProps.getTemplate()).isEqualTo(NotifyTemplate.APPLICATION_SUBMITTED);
      assertThat(emailProps.getEmailPersonalisation()).contains(
          entry("APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef()),
          entry("APPLICATION_TYPE", pwaApplicationDetail.getPwaApplicationType().getDisplayName()),
          entry("CASE_MANAGEMENT_LINK", caseManagementLink)
      );

    });

  }

  @Test
  void getSubmissionType() {
    assertThat(pwaApplicationFirstDraftSubmissionService.getSubmissionType()).isEqualTo(ApplicationSubmissionType.FIRST_DRAFT);
  }

}