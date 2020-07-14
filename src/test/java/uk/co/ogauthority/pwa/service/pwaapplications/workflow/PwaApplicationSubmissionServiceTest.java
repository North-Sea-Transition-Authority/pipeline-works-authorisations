package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDataCleanupService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationSubmissionServiceTest {

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private TeamService teamService;

  @Mock
  private PwaApplicationDataCleanupService dataCleanupService;

  @Captor
  private ArgumentCaptor<EmailProperties> emailPropsCaptor;

  private PwaApplicationSubmissionService pwaApplicationSubmissionService;

  private PwaApplicationDetail pwaApplicationDetail;
  private WebUserAccount user = new WebUserAccount(1);

  @Before
  public void setup() {
    pwaApplicationSubmissionService = new PwaApplicationSubmissionService(
        pwaApplicationDetailService,
        camundaWorkflowService,
        notifyService,
        teamService,
        dataCleanupService
    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);


  }


  @Test(expected = IllegalArgumentException.class)
  public void submitApplication_whenDetailIsNotTip() {
    pwaApplicationDetail.setTipFlag(false);
    pwaApplicationSubmissionService.submitApplication(user, pwaApplicationDetail);

  }


  @Test
  public void submitApplication_whenDetailIsNotDraft() {
    var invalidSubmitStatuses = EnumSet.allOf(PwaApplicationStatus.class);
    invalidSubmitStatuses.remove(PwaApplicationStatus.DRAFT);

    // test each status where error expected
    for (PwaApplicationStatus invalidStatus : invalidSubmitStatuses) {

      PwaApplicationTestUtil.tryAssertionWithStatus(
          invalidStatus,
          (status) -> {
            pwaApplicationDetail.setStatus(status);
            assertThatThrownBy(() ->
                pwaApplicationSubmissionService.submitApplication(user, pwaApplicationDetail)).isInstanceOf(
                IllegalArgumentException.class);
          }
      );
    }
  }


  @Test
  public void submitApplication_whenDetailIsTipDraft() {

    var teamMemberList = List.of(
        TeamTestingUtils.createRegulatorTeamMember(teamService.getRegulatorTeam(),
          new Person(1, "PWA", "Manager1", "manager1@pwa.co.uk", null),
          Set.of(PwaRegulatorRole.PWA_MANAGER)),
        TeamTestingUtils.createRegulatorTeamMember(teamService.getRegulatorTeam(),
          new Person(2, "PWA", "Manager2", "manager2@pwa.co.uk", null),
          Set.of(PwaRegulatorRole.PWA_MANAGER)),
        TeamTestingUtils.createRegulatorTeamMember(teamService.getRegulatorTeam(),
          new Person(3, "PWA", "Case officer", "co@pwa.co.uk", null),
          Set.of(PwaRegulatorRole.CASE_OFFICER))
    );

    when(teamService.getTeamMembers(teamService.getRegulatorTeam())).thenReturn(teamMemberList);

    pwaApplicationSubmissionService.submitApplication(user, pwaApplicationDetail);

    verify(dataCleanupService, times(1)).cleanupData(pwaApplicationDetail);

    verify(pwaApplicationDetailService, times(1)).setSubmitted(pwaApplicationDetail, user);
    verify(camundaWorkflowService, times(1)).completeTask(eq(new WorkflowTaskInstance(pwaApplicationDetail.getPwaApplication(), PwaApplicationWorkflowTask.PREPARE_APPLICATION)));

    verify(notifyService, times(1)).sendEmail(emailPropsCaptor.capture(), eq("manager1@pwa.co.uk"));
    assertThat(emailPropsCaptor.getValue().getEmailPersonalisation().get("RECIPIENT_FULL_NAME")).isEqualTo("PWA Manager1");

    verify(notifyService, times(1)).sendEmail(emailPropsCaptor.capture(), eq("manager2@pwa.co.uk"));
    assertThat(emailPropsCaptor.getValue().getEmailPersonalisation().get("RECIPIENT_FULL_NAME")).isEqualTo("PWA Manager2");

    emailPropsCaptor.getAllValues().forEach(emailProps -> {

      assertThat(emailProps.getTemplate()).isEqualTo(NotifyTemplate.APPLICATION_SUBMITTED);
      assertThat(emailProps.getEmailPersonalisation()).contains(
          entry("APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef()),
          entry("APPLICATION_TYPE", pwaApplicationDetail.getPwaApplicationType().getDisplayName())
      );

    });

  }


}