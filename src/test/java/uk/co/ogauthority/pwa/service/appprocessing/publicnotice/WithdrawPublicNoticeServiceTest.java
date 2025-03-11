package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticeWithdrawnEmailProps;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.publicnotice.WithdrawPublicNoticeForm;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.publicnotice.WithdrawPublicNoticeValidator;

@ExtendWith(MockitoExtension.class)
class WithdrawPublicNoticeServiceTest {


  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private WithdrawPublicNoticeValidator validator;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private PwaTeamService pwaTeamService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private Clock clock;

  @InjectMocks
  private WithdrawPublicNoticeService withdrawPublicNoticeService;

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;


  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;


  @BeforeEach
  void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  void publicNoticeCanBeWithdrawn_withdrawablePublicNoticeExistsForApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getOpenPublicNotices()).thenReturn(List.of(publicNotice));
    var publicNoticeCanBeWithdrawn = withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(pwaApplication);
    assertThat(publicNoticeCanBeWithdrawn).isTrue();
  }

  @Test
  void publicNoticeCanBeWithdrawn_withdrawablePublicNoticeExistsForDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(new PwaApplication());
    when(publicNoticeService.getOpenPublicNotices()).thenReturn(List.of(publicNotice));
    var publicNoticeCanBeWithdrawn = withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(pwaApplication);
    assertThat(publicNoticeCanBeWithdrawn).isFalse();
  }

  @Test
  void publicNoticeCanBeWithdrawn_withdrawablePublicNoticeDoesNotExist() {
    when(publicNoticeService.getOpenPublicNotices()).thenReturn(List.of());
    var publicNoticeCanBeWithdrawn = withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(pwaApplication);
    assertThat(publicNoticeCanBeWithdrawn).isFalse();
  }

  @Test
  void validate_verifyServiceInteractions() {

    var form = new WithdrawPublicNoticeForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    withdrawPublicNoticeService.validate(form, bindingResult);
    verify(validator).validate(form, bindingResult);
  }

  @Test
  void withdrawPublicNotice_publicNoticeAtApprovalStage_workflowEndedAndEmailSentToManagerOnly() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    var form = new WithdrawPublicNoticeForm();
    form.setWithdrawalReason("my reason");

    var withdrawnPublicNotice = PublicNoticeTestUtil.createWithdrawnPublicNotice(
        pwaApplication, user.getLinkedPerson(), form.getWithdrawalReason(), clock.instant());
    when(publicNoticeService.savePublicNotice(publicNotice)).thenReturn(withdrawnPublicNotice);

    var latestPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeDocument(publicNotice)).thenReturn(latestPublicNoticeDocument);

    var emailRecipients = Set.of(PersonTestUtil.createPersonFrom(new PersonId(200), "manager@email.com"));
    when(pwaTeamService.getPeopleWithRegulatorRole(Role.PWA_MANAGER)).thenReturn(emailRecipients);

    withdrawPublicNoticeService.withdrawPublicNotice(pwaApplication, form, user);

    verify(camundaWorkflowService).deleteProcessAndTask(new WorkflowTaskInstance(
        publicNotice, PwaApplicationPublicNoticeWorkflowTask.MANAGER_APPROVAL));

    verify(publicNoticeService).archivePublicNoticeDocument(latestPublicNoticeDocument);

    verify(publicNoticeService).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.WITHDRAWN);
    assertThat(actualPublicNotice.getWithdrawalReason()).isEqualTo(withdrawnPublicNotice.getWithdrawalReason());
    assertThat(actualPublicNotice.getWithdrawalTimestamp()).isEqualTo(withdrawnPublicNotice.getWithdrawalTimestamp());
    assertThat(actualPublicNotice.getWithdrawingPersonId()).isEqualTo(withdrawnPublicNotice.getWithdrawingPersonId());

    emailRecipients.forEach(pwaManager -> {
      var expectedEmailProps = new PublicNoticeWithdrawnEmailProps(
          pwaManager.getFullName(),
          pwaApplication.getAppReference(),
          user.getLinkedPerson().getFullName(),
          form.getWithdrawalReason());

      verify(notifyService).sendEmail(expectedEmailProps, pwaManager.getEmailAddress());
    });
    verify(notifyService, times(emailRecipients.size())).sendEmail(any(), any());
  }

  @Test
  void withdrawPublicNotice_publicNoticeAtApprovalStage_workflowEndedAndEmailSentToManagerOnly_noDoc_noError() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    var form = new WithdrawPublicNoticeForm();
    form.setWithdrawalReason("my reason");

    var withdrawnPublicNotice = PublicNoticeTestUtil.createWithdrawnPublicNotice(
        pwaApplication, user.getLinkedPerson(), form.getWithdrawalReason(), clock.instant());
    when(publicNoticeService.savePublicNotice(publicNotice)).thenReturn(withdrawnPublicNotice);

    when(publicNoticeService.getLatestPublicNoticeDocument(publicNotice))
        .thenThrow(EntityLatestVersionNotFoundException.class);

    var emailRecipients = Set.of(PersonTestUtil.createPersonFrom(new PersonId(200), "manager@email.com"));
    when(pwaTeamService.getPeopleWithRegulatorRole(Role.PWA_MANAGER)).thenReturn(emailRecipients);

    withdrawPublicNoticeService.withdrawPublicNotice(pwaApplication, form, user);

    verify(camundaWorkflowService).deleteProcessAndTask(new WorkflowTaskInstance(
        publicNotice, PwaApplicationPublicNoticeWorkflowTask.MANAGER_APPROVAL));

    verify(publicNoticeService, Mockito.never()).archivePublicNoticeDocument(any());

    verify(publicNoticeService).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.WITHDRAWN);
    assertThat(actualPublicNotice.getWithdrawalReason()).isEqualTo(withdrawnPublicNotice.getWithdrawalReason());
    assertThat(actualPublicNotice.getWithdrawalTimestamp()).isEqualTo(withdrawnPublicNotice.getWithdrawalTimestamp());
    assertThat(actualPublicNotice.getWithdrawingPersonId()).isEqualTo(withdrawnPublicNotice.getWithdrawingPersonId());

    emailRecipients.forEach(pwaManager -> {
      var expectedEmailProps = new PublicNoticeWithdrawnEmailProps(
          pwaManager.getFullName(),
          pwaApplication.getAppReference(),
          user.getLinkedPerson().getFullName(),
          form.getWithdrawalReason());

      verify(notifyService).sendEmail(expectedEmailProps, pwaManager.getEmailAddress());
    });
    verify(notifyService, times(emailRecipients.size())).sendEmail(any(), any());
  }

  @Test
  void withdrawPublicNotice_publicNoticeAtCaseOfficerReviewStage_workflowEndedAndEmailSentToManagerAndApplicant() {

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    var form = new WithdrawPublicNoticeForm();
    form.setWithdrawalReason("my reason");

    var withdrawnPublicNotice = PublicNoticeTestUtil.createWithdrawnPublicNotice(
        pwaApplication, user.getLinkedPerson(), form.getWithdrawalReason(), clock.instant());
    when(publicNoticeService.savePublicNotice(publicNotice)).thenReturn(withdrawnPublicNotice);

    var latestPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeDocument(publicNotice)).thenReturn(latestPublicNoticeDocument);

    var pwaManager = PersonTestUtil.createPersonFrom(new PersonId(200), "manager@email.com");
    when(pwaTeamService.getPeopleWithRegulatorRole(Role.PWA_MANAGER)).thenReturn(Set.of(pwaManager));

    var applicant = PersonTestUtil.createPersonFrom(new PersonId(300), "applicant@email.com");
    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplication,
        PwaContactRole.PREPARER)).thenReturn(List.of(applicant));
    var emailRecipients = List.of(pwaManager, applicant);

    withdrawPublicNoticeService.withdrawPublicNotice(pwaApplication, form, user);

    verify(camundaWorkflowService).deleteProcessAndTask(new WorkflowTaskInstance(
        publicNotice, PwaApplicationPublicNoticeWorkflowTask.CASE_OFFICER_REVIEW));

    verify(publicNoticeService).archivePublicNoticeDocument(latestPublicNoticeDocument);

    emailRecipients.forEach(recipient -> {
      var expectedEmailProps = new PublicNoticeWithdrawnEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          user.getLinkedPerson().getFullName(),
          form.getWithdrawalReason());

      verify(notifyService).sendEmail(expectedEmailProps, recipient.getEmailAddress());
    });
  }

  @Test
  void withdrawPublicNotice_publishedStatus_workflowUnchanged() {

    var publicNotice = PublicNoticeTestUtil.createPublishedPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var form = new WithdrawPublicNoticeForm();
    form.setWithdrawalReason("my reason");
    withdrawPublicNoticeService.withdrawPublicNotice(pwaApplication, form, user);

    verifyNoInteractions(camundaWorkflowService);
  }
}
