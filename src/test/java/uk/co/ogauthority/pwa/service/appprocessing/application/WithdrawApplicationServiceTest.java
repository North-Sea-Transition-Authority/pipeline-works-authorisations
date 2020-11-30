package uk.co.ogauthority.pwa.service.appprocessing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.withdraw.WithdrawApplicationForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ApplicationWithdrawnEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.validators.WithdrawApplicationValidator;

@RunWith(MockitoJUnitRunner.class)
public class WithdrawApplicationServiceTest {

  private WithdrawApplicationService withdrawApplicationService;

  @Mock
  private WithdrawApplicationValidator withdrawApplicationValidator;
  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;
  @Mock
  private CamundaWorkflowService camundaWorkflowService;
  @Mock
  private ConsultationRequestService consultationRequestService;
  @Mock
  private NotifyService notifyService;
  @Mock
  private PwaContactService pwaContactService;

  private PwaApplicationDetail pwaApplicationDetail;


  @Before
  public void setUp() {
    withdrawApplicationService = new WithdrawApplicationService(
        withdrawApplicationValidator,
        pwaApplicationDetailService,
        camundaWorkflowService,
        consultationRequestService,
        notifyService,
        pwaContactService);

    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    pwaApplicationDetail = new PwaApplicationDetail(pwaApplication, null, null, null);
  }


  @Test
  public void withdrawApplication() {

    var withdrawingPerson = PersonTestUtil.createDefaultPerson();
    var withdrawingUser = new AuthenticatedUserAccount(new WebUserAccount(1, withdrawingPerson), List.of());
    var form = new WithdrawApplicationForm();
    form.setWithdrawalReason("my reason");


    var workflowTaskInstance = new WorkflowTaskInstance(pwaApplicationDetail.getPwaApplication(), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW);
    when(camundaWorkflowService.getAllActiveWorkflowTasks(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(Set.of(workflowTaskInstance));

    var consultationRequest = new ConsultationRequest();
    when(consultationRequestService.getAllOpenRequestsByApplication(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(List.of(consultationRequest));

    var appPerson = PersonTestUtil.createDefaultPerson();
    when(pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER
    )).thenReturn(List.of(withdrawingPerson, appPerson));

    var emailProps = new ApplicationWithdrawnEmailProps(
        appPerson.getFullName(), pwaApplicationDetail.getPwaApplicationRef(), withdrawingUser.getFullName());


    withdrawApplicationService.withdrawApplication(form, pwaApplicationDetail, withdrawingUser);

    verify(camundaWorkflowService, times(1)).deleteProcessInstanceAndThenTasks(pwaApplicationDetail.getPwaApplication());
    verify(consultationRequestService, times(1)).withdrawConsultationRequest(consultationRequest, withdrawingUser);
    verify(notifyService, times(2)).sendEmail(any(), any());
    verify(notifyService, atLeastOnce()).sendEmail(emailProps, appPerson.getEmailAddress());
    verify(notifyService, atLeastOnce()).sendEmail(emailProps, withdrawingPerson.getEmailAddress());
  }


  @Test
  public void canShowInTaskList() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.WITHDRAW_APPLICATION), null,
        null);

    boolean canShow = withdrawApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_industry() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null,
        null);

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
