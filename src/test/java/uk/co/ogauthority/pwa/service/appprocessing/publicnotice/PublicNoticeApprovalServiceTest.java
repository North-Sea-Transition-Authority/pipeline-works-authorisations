package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeApprovalForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticeApprovedEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticeRejectedEmailProps;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeApprovalValidator;

@RunWith(MockitoJUnitRunner.class)
public class PublicNoticeApprovalServiceTest {

  private PublicNoticeApprovalService publicNoticeApprovalService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private PublicNoticeApprovalValidator validator;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private Clock clock;

  @Mock
  private NotifyService notifyService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Mock
  private TeamService teamService;

  @Mock
  private PwaContactService pwaContactService;

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeRequest> publicNoticeRequestArgumentCaptor;


  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;



  @Before
  public void setUp() {

    publicNoticeApprovalService = new PublicNoticeApprovalService(publicNoticeService, validator,
        camundaWorkflowService, clock, notifyService, emailCaseLinkService, teamService, pwaContactService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  public void openPublicNoticeCanBeApproved_approvablePublicNoticeExistsWithApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.MANAGER_APPROVAL)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeApprovalService.openPublicNoticeCanBeApproved(pwaApplication);
    assertThat(publicNoticeExists).isTrue();
  }

  @Test
  public void openPublicNoticeCanBeApproved_approvablePublicNoticeExistsWithDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(new PwaApplication());
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.MANAGER_APPROVAL)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeApprovalService.openPublicNoticeCanBeApproved(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  public void openPublicNoticeCanBeApproved_approvablePublicNoticeDoesNotExist() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.MANAGER_APPROVAL)).thenReturn(List.of());
    var publicNoticeExists = publicNoticeApprovalService.openPublicNoticeCanBeApproved(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }


  @Test
  public void updatePublicNoticeRequest_requestApproved() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var publicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeRequest(publicNotice)).thenReturn(publicNoticeRequest);

    String caseManagementLink = "case management link url";
    when(emailCaseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);
    var emailRecipients = List.of(PersonTestUtil.createDefaultPerson());
    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplication, PwaContactRole.PREPARER))
        .thenReturn(emailRecipients);

    var form = PublicNoticeApprovalTestUtil.createApprovedPublicNoticeForm();
    publicNoticeApprovalService.updatePublicNoticeRequest(form, pwaApplication, user);


    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.APPLICANT_UPDATE);

    verify(publicNoticeService, times(1)).savePublicNoticeRequest(publicNoticeRequestArgumentCaptor.capture());
    var actualPublicNoticeRequest = publicNoticeRequestArgumentCaptor.getValue();
    assertThat(actualPublicNoticeRequest.getRequestApproved()).isTrue();
    assertThat(actualPublicNoticeRequest.getResponderPersonId()).isEqualTo(user.getLinkedPerson().getId().asInt());
    assertThat(actualPublicNoticeRequest.getResponseTimestamp()).isEqualTo(clock.instant());

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(publicNotice, form.getRequestApproved());
    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.MANAGER_APPROVAL));

    emailRecipients.forEach(recipient -> {
      var expectedEmailProps = new PublicNoticeApprovedEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          caseManagementLink);

      verify(notifyService, times(1)).sendEmail(expectedEmailProps, recipient.getEmailAddress());
    });
  }

  @Test
  public void updatePublicNoticeRequest_requestRejected() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var publicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeRequest(publicNotice)).thenReturn(publicNoticeRequest);

    String caseManagementLink = "case management link url";
    when(emailCaseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);
    var regulatorTeam = TeamTestingUtils.getRegulatorTeam();
    when(teamService.getRegulatorTeam()).thenReturn(regulatorTeam);
    var regulatorTeamMember = TeamTestingUtils.createRegulatorTeamMember(
        regulatorTeam, PersonTestUtil.createDefaultPerson(), Set.of(PwaRegulatorRole.CASE_OFFICER));
    var regulatorTeamMembers = List.of(regulatorTeamMember);
    when(teamService.getTeamMembers(regulatorTeam)).thenReturn(regulatorTeamMembers);

    var form = PublicNoticeApprovalTestUtil.createRejectedPublicNoticeForm();
    publicNoticeApprovalService.updatePublicNoticeRequest(form, pwaApplication, user);


    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.DRAFT);

    verify(publicNoticeService, times(1)).savePublicNoticeRequest(publicNoticeRequestArgumentCaptor.capture());
    var actualPublicNoticeRequest = publicNoticeRequestArgumentCaptor.getValue();
    assertThat(actualPublicNoticeRequest.getRequestApproved()).isFalse();
    assertThat(actualPublicNoticeRequest.getResponderPersonId()).isEqualTo(user.getLinkedPerson().getId().asInt());
    assertThat(actualPublicNoticeRequest.getResponseTimestamp()).isEqualTo(clock.instant());
    assertThat(actualPublicNoticeRequest.getRejectionReason()).isEqualTo(form.getRequestRejectedReason());

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(publicNotice, form.getRequestApproved());
    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.MANAGER_APPROVAL));

    regulatorTeamMembers.forEach(teamMember -> {
      var expectedEmailProps = new PublicNoticeRejectedEmailProps(
          teamMember.getPerson().getFullName(),
          pwaApplication.getAppReference(),
          form.getRequestRejectedReason(),
          caseManagementLink);

      verify(notifyService, times(1)).sendEmail(expectedEmailProps, teamMember.getPerson().getEmailAddress());
    });

  }


  @Test
  public void validate_verifyServiceInteractions() {

    var form = new PublicNoticeApprovalForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    publicNoticeApprovalService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);

  }


}
