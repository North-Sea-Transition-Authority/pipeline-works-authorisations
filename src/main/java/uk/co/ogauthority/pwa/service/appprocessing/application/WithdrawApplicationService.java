package uk.co.ogauthority.pwa.service.appprocessing.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.withdraw.WithdrawApplicationForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.ApplicationWithdrawnEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.consultations.WithdrawConsultationService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.validators.WithdrawApplicationValidator;

@Service
public class WithdrawApplicationService implements AppProcessingService {

  private final WithdrawApplicationValidator withdrawApplicationValidator;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final WithdrawConsultationService withdrawConsultationService;
  private final NotifyService notifyService;
  private final PwaContactService pwaContactService;
  private final EmailCaseLinkService emailCaseLinkService;

  @Autowired
  public WithdrawApplicationService(
      WithdrawApplicationValidator withdrawApplicationValidator,
      PwaApplicationDetailService pwaApplicationDetailService,
      CamundaWorkflowService camundaWorkflowService,
      WithdrawConsultationService withdrawConsultationService,
      NotifyService notifyService,
      PwaContactService pwaContactService,
      EmailCaseLinkService emailCaseLinkService) {
    this.withdrawApplicationValidator = withdrawApplicationValidator;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.withdrawConsultationService = withdrawConsultationService;
    this.notifyService = notifyService;
    this.pwaContactService = pwaContactService;
    this.emailCaseLinkService = emailCaseLinkService;
  }


  public void withdrawApplication(WithdrawApplicationForm form,
                                  PwaApplicationDetail pwaApplicationDetail, AuthenticatedUserAccount withdrawingUser) {

    pwaApplicationDetailService.setWithdrawn(pwaApplicationDetail, withdrawingUser.getLinkedPerson(), form.getWithdrawalReason());
    camundaWorkflowService.deleteProcessInstanceAndThenTasks(pwaApplicationDetail.getPwaApplication());

    withdrawConsultationService.withdrawAllOpenConsultationRequests(pwaApplicationDetail.getPwaApplication(), withdrawingUser);
    sendWithdrawalEmails(pwaApplicationDetail, withdrawingUser);
  }

  private void sendWithdrawalEmails(PwaApplicationDetail pwaApplicationDetail, AuthenticatedUserAccount withdrawingUser) {
    var emailRecipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER
    );
    emailRecipients.forEach(recipient -> {
      var emailProps = new ApplicationWithdrawnEmailProps(
          recipient.getFullName(),
          pwaApplicationDetail.getPwaApplicationRef(),
          withdrawingUser.getFullName(),
          emailCaseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication())
      );
      notifyService.sendEmail(emailProps, recipient.getEmailAddress());
    });
  }


  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return !ApplicationState.ENDED.includes(processingContext.getApplicationDetailStatus())
        && processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.WITHDRAW_APPLICATION);
  }

  public BindingResult validate(Object form, BindingResult bindingResult,
                                PwaApplicationDetail pwaApplicationDetail) {
    withdrawApplicationValidator.validate(form, bindingResult, pwaApplicationDetail);
    return bindingResult;
  }

}
