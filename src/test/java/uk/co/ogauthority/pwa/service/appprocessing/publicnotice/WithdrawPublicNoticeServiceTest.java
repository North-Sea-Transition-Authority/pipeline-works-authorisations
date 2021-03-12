package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.publicnotice.WithdrawPublicNoticeForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticeWithdrawnEmailProps;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.publicnotice.WithdrawPublicNoticeValidator;

@RunWith(MockitoJUnitRunner.class)
public class WithdrawPublicNoticeServiceTest {

  private WithdrawPublicNoticeService withdrawPublicNoticeService;

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

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;


  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;



  @Before
  public void setUp() {

    withdrawPublicNoticeService = new WithdrawPublicNoticeService(publicNoticeService, validator,
        camundaWorkflowService, pwaTeamService, pwaContactService, notifyService, clock);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  public void publicNoticeCanBeWithdrawn_withdrawablePublicNoticeExistsForApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getOpenPublicNotices()).thenReturn(List.of(publicNotice));
    var publicNoticeCanBeWithdrawn = withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(pwaApplication);
    assertThat(publicNoticeCanBeWithdrawn).isTrue();
  }

  @Test
  public void publicNoticeCanBeWithdrawn_withdrawablePublicNoticeExistsForDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(new PwaApplication());
    when(publicNoticeService.getOpenPublicNotices()).thenReturn(List.of(publicNotice));
    var publicNoticeCanBeWithdrawn = withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(pwaApplication);
    assertThat(publicNoticeCanBeWithdrawn).isFalse();
  }

  @Test
  public void publicNoticeCanBeWithdrawn_withdrawablePublicNoticeDoesNotExist() {
    when(publicNoticeService.getOpenPublicNotices()).thenReturn(List.of());
    var publicNoticeCanBeWithdrawn = withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(pwaApplication);
    assertThat(publicNoticeCanBeWithdrawn).isFalse();
  }

  @Test
  public void validate_verifyServiceInteractions() {

    var form = new WithdrawPublicNoticeForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    withdrawPublicNoticeService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);
  }

  @Test
  public void withdrawPublicNotice_publicNoticeAtApprovalStage_workflowEndedAndEmailSentToManagerOnly() {

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
    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER)).thenReturn(emailRecipients);

    withdrawPublicNoticeService.withdrawPublicNotice(pwaApplication, form, user);

    verify(camundaWorkflowService, times(1)).deleteProcessAndTask(new WorkflowTaskInstance(
        publicNotice, PwaApplicationPublicNoticeWorkflowTask.MANAGER_APPROVAL));

    verify(publicNoticeService, times(1)).archivePublicNoticeDocument(latestPublicNoticeDocument);

    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
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

      verify(notifyService, times(1)).sendEmail(expectedEmailProps, pwaManager.getEmailAddress());
    });
    verify(notifyService, times(emailRecipients.size())).sendEmail(any(), any());
  }

  @Test
  public void withdrawPublicNotice_publicNoticeAtCaseOfficerReviewStage_workflowEndedAndEmailSentToManagerAndApplicant() {

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
    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER)).thenReturn(Set.of(pwaManager));

    var applicant = PersonTestUtil.createPersonFrom(new PersonId(300), "applicant@email.com");
    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplication,
        PwaContactRole.PREPARER)).thenReturn(List.of(applicant));
    var emailRecipients = List.of(pwaManager, applicant);

    withdrawPublicNoticeService.withdrawPublicNotice(pwaApplication, form, user);

    verify(camundaWorkflowService, times(1)).deleteProcessAndTask(new WorkflowTaskInstance(
        publicNotice, PwaApplicationPublicNoticeWorkflowTask.CASE_OFFICER_REVIEW));

    verify(publicNoticeService, times(1)).archivePublicNoticeDocument(latestPublicNoticeDocument);

    emailRecipients.forEach(recipient -> {
      var expectedEmailProps = new PublicNoticeWithdrawnEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          user.getLinkedPerson().getFullName(),
          form.getWithdrawalReason());

      verify(notifyService, times(1)).sendEmail(expectedEmailProps, recipient.getEmailAddress());
    });
  }


}
