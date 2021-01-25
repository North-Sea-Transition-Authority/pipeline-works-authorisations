package uk.co.ogauthority.pwa.service.appprocessing.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.withdraw.WithdrawApplicationForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ApplicationWithdrawnEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.validators.WithdrawApplicationValidator;

@Service
public class WithdrawApplicationService implements AppProcessingService {

  private final WithdrawApplicationValidator withdrawApplicationValidator;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final ConsultationRequestService consultationRequestService;
  private final NotifyService notifyService;
  private final PwaContactService pwaContactService;

  @Autowired
  public WithdrawApplicationService(
      WithdrawApplicationValidator withdrawApplicationValidator,
      PwaApplicationDetailService pwaApplicationDetailService,
      CamundaWorkflowService camundaWorkflowService,
      ConsultationRequestService consultationRequestService,
      NotifyService notifyService,
      PwaContactService pwaContactService) {
    this.withdrawApplicationValidator = withdrawApplicationValidator;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.consultationRequestService = consultationRequestService;
    this.notifyService = notifyService;
    this.pwaContactService = pwaContactService;
  }


  public void withdrawApplication(WithdrawApplicationForm form,
                                  PwaApplicationDetail pwaApplicationDetail, AuthenticatedUserAccount withdrawingUser) {

    pwaApplicationDetailService.setWithdrawn(pwaApplicationDetail, withdrawingUser.getLinkedPerson(), form.getWithdrawalReason());
    camundaWorkflowService.deleteProcessInstanceAndThenTasks(pwaApplicationDetail.getPwaApplication());

    consultationRequestService.withdrawAllOpenConsultationRequests(pwaApplicationDetail.getPwaApplication(), withdrawingUser);
    sendWithdrawalEmails(pwaApplicationDetail, withdrawingUser);
  }

  private void sendWithdrawalEmails(PwaApplicationDetail pwaApplicationDetail, AuthenticatedUserAccount withdrawingUser) {
    var emailRecipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER
    );
    emailRecipients.forEach(recipient -> {
      var emailProps = new ApplicationWithdrawnEmailProps(
          recipient.getFullName(), pwaApplicationDetail.getPwaApplicationRef(), withdrawingUser.getFullName());
      notifyService.sendEmail(emailProps, recipient.getEmailAddress());
    });
  }


  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.WITHDRAW_APPLICATION);
  }

  public BindingResult validate(Object form, BindingResult bindingResult,
                                PwaApplicationDetail pwaApplicationDetail) {
    withdrawApplicationValidator.validate(form, bindingResult, pwaApplicationDetail);
    return bindingResult;
  }

}
