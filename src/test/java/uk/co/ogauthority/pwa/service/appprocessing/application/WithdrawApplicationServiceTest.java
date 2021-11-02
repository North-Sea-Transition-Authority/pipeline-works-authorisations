package uk.co.ogauthority.pwa.service.appprocessing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
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
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.exception.WithdrawApplicationException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.withdraw.WithdrawApplicationForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.ApplicationWithdrawnEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.WithdrawConsultationService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.validators.WithdrawApplicationValidator;

@RunWith(MockitoJUnitRunner.class)
public class WithdrawApplicationServiceTest {

  private WithdrawApplicationService withdrawApplicationService;

  @Mock
  private WithdrawApplicationValidator withdrawApplicationValidator;
  @Mock
  private PwaApplicationService pwaApplicationService;
  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;
  @Mock
  private CamundaWorkflowService camundaWorkflowService;
  @Mock
  private WithdrawConsultationService withdrawConsultationService;
  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;
  @Mock
  private NotifyService notifyService;
  @Mock
  private PwaContactService pwaContactService;
  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Captor
  private ArgumentCaptor<PwaApplicationDetail> appDetailDeleteCaptor;
  @Captor
  private ArgumentCaptor<PwaApplicationDetail> appDetailWithdrawCaptor;
  @Captor
  private ArgumentCaptor<Person> personCaptor;
  @Captor
  private ArgumentCaptor<String> withdrawReasonCaptor;
  @Captor
  private ArgumentCaptor<Consumer<PwaApplicationDetail>> pwaLastSubmittedDetailConsumerCaptor;
  @Captor
  private ArgumentCaptor<Consumer<PwaApplicationDetail>> pwaUpdateRequestedDetailConsumerCaptor;

  private PwaApplicationDetail pwaApplicationDetail;
  private PwaApplication pwaApplication;


  @Before
  public void setUp() {
    withdrawApplicationService = new WithdrawApplicationService(
        withdrawApplicationValidator,
        pwaApplicationService,
        pwaApplicationDetailService,
        camundaWorkflowService,
        withdrawConsultationService,
        applicationUpdateRequestService,
        notifyService,
        pwaContactService,
        emailCaseLinkService);

    pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    pwaApplicationDetail = new PwaApplicationDetail(pwaApplication, null, null, null);
  }


  @Test
  public void withdrawApplication() {

    var withdrawingPerson = PersonTestUtil.createDefaultPerson();
    var withdrawingUser = new AuthenticatedUserAccount(new WebUserAccount(1, withdrawingPerson), List.of());
    var form = new WithdrawApplicationForm();
    form.setWithdrawalReason("my reason");

    var appPerson = PersonTestUtil.createDefaultPerson();
    when(pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER
    )).thenReturn(List.of(withdrawingPerson, appPerson));

    var caseManagementLink = "case management link url";
    when(emailCaseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication())).thenReturn(caseManagementLink);

    var emailProps = new ApplicationWithdrawnEmailProps(
        appPerson.getFullName(), pwaApplicationDetail.getPwaApplicationRef(), withdrawingUser.getFullName(),
        caseManagementLink);

    withdrawApplicationService.withdrawApplication(form, pwaApplication, withdrawingUser);

    verify(camundaWorkflowService, times(1)).deleteProcessInstanceAndThenTasks(pwaApplicationDetail.getPwaApplication());
    verify(withdrawConsultationService, times(1)).withdrawAllOpenConsultationRequests(pwaApplicationDetail.getPwaApplication(), withdrawingUser);
    verify(notifyService, times(2)).sendEmail(any(), any());
    verify(notifyService, atLeastOnce()).sendEmail(emailProps, appPerson.getEmailAddress());
    verify(notifyService, atLeastOnce()).sendEmail(emailProps, withdrawingPerson.getEmailAddress());
    verify(pwaApplicationDetailService).doWithLastSubmittedDetailIfExists(eq(pwaApplication), any(Consumer.class));
    verify(pwaApplicationDetailService).doWithCurrentUpdateRequestedDetailIfExists(eq(pwaApplication), any(Consumer.class));
  }


  @Test
  public void withdrawApplication_verifyLastSubmittedDetailHandlerFunctionCalled_verifyOtherServiceInteractions() {

    var withdrawingPerson = PersonTestUtil.createDefaultPerson();
    var withdrawingUser = new AuthenticatedUserAccount(new WebUserAccount(1, withdrawingPerson), List.of());
    var form = new WithdrawApplicationForm();
    form.setWithdrawalReason("my reason");

    withdrawApplicationService.withdrawApplication(form, pwaApplication, withdrawingUser);

    verify(pwaApplicationDetailService).doWithLastSubmittedDetailIfExists(
        eq(pwaApplication), pwaLastSubmittedDetailConsumerCaptor.capture());

    pwaLastSubmittedDetailConsumerCaptor.getValue().accept(pwaApplicationDetail);
    verify(pwaApplicationDetailService).setWithdrawn(pwaApplicationDetail, withdrawingPerson, form.getWithdrawalReason());
    verify(pwaApplicationDetailService).doWithLastSubmittedDetailIfExists(eq(pwaApplication), any(Consumer.class));
  }

  @Test
  public void withdrawApplication_updateRequestExists_verifyUpdateRequestedDetailHandlerFunctionCalled_verifyOtherServiceInteractions() {

    var withdrawingUser = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
    var form = new WithdrawApplicationForm();
    form.setWithdrawalReason("my reason");

    var lastSubmittedDetail = new PwaApplicationDetail();
    when(pwaApplicationDetailService.getLatestSubmittedDetail(pwaApplication)).thenReturn(Optional.of(lastSubmittedDetail));

    withdrawApplicationService.withdrawApplication(form, pwaApplication, withdrawingUser);

    verify(pwaApplicationDetailService).doWithCurrentUpdateRequestedDetailIfExists(
        eq(pwaApplication), pwaUpdateRequestedDetailConsumerCaptor.capture());

    pwaUpdateRequestedDetailConsumerCaptor.getValue().accept(pwaApplicationDetail);
    verify(applicationUpdateRequestService, times(1)).endUpdateRequestIfExists(pwaApplicationDetail);
    verify(pwaApplicationDetailService, times(1)).transferTipFlag(pwaApplicationDetail, lastSubmittedDetail);
  }

  @Test(expected = WithdrawApplicationException.class)
  public void withdrawApplication_updateRequestExists_lastSubmittedDetailNotFound_error() {

    var withdrawingUser = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
    var form = new WithdrawApplicationForm();
    form.setWithdrawalReason("my reason");

    when(pwaApplicationDetailService.getLatestSubmittedDetail(pwaApplication)).thenReturn(Optional.empty());

    withdrawApplicationService.withdrawApplication(form, pwaApplication, withdrawingUser);

    verify(pwaApplicationDetailService).doWithCurrentUpdateRequestedDetailIfExists(
        eq(pwaApplication), pwaUpdateRequestedDetailConsumerCaptor.capture());

    pwaUpdateRequestedDetailConsumerCaptor.getValue().accept(pwaApplicationDetail);
  }


  @Test
  public void canShowInTaskList_appNotEnded_hasPermission_true() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(PwaAppProcessingPermission.WITHDRAW_APPLICATION), null,
        null, Set.of());

    boolean canShow = withdrawApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_appNotEnded_doesNotHavePermission_false() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null,
        null, Set.of());

    boolean canShow = withdrawApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_appEnded_false() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.COMPLETE);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null,
        null, Set.of());

    boolean canShow = withdrawApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void validate() {

    var form = new WithdrawApplicationForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    withdrawApplicationService.validate(form, bindingResult, new PwaApplicationDetail());
    verify(withdrawApplicationValidator, times(1)).validate(form, bindingResult, new PwaApplicationDetail());
  }


}
