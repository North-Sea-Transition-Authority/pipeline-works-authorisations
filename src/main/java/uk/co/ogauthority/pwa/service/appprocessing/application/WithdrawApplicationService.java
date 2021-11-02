package uk.co.ogauthority.pwa.service.appprocessing.application;

import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.WithdrawApplicationException;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.withdraw.WithdrawApplicationForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.ApplicationWithdrawnEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.consultations.WithdrawConsultationService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.validators.WithdrawApplicationValidator;

@Service
public class WithdrawApplicationService implements AppProcessingService {

  private final WithdrawApplicationValidator withdrawApplicationValidator;
  private final PwaApplicationService pwaApplicationService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final WithdrawConsultationService withdrawConsultationService;
  private final ApplicationUpdateRequestService applicationUpdateRequestService;
  private final NotifyService notifyService;
  private final PwaContactService pwaContactService;
  private final EmailCaseLinkService emailCaseLinkService;

  @Autowired
  public WithdrawApplicationService(
      WithdrawApplicationValidator withdrawApplicationValidator,
      PwaApplicationService pwaApplicationService,
      PwaApplicationDetailService pwaApplicationDetailService,
      CamundaWorkflowService camundaWorkflowService,
      WithdrawConsultationService withdrawConsultationService,
      ApplicationUpdateRequestService applicationUpdateRequestService,
      NotifyService notifyService,
      PwaContactService pwaContactService,
      EmailCaseLinkService emailCaseLinkService) {
    this.withdrawApplicationValidator = withdrawApplicationValidator;
    this.pwaApplicationService = pwaApplicationService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.withdrawConsultationService = withdrawConsultationService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.notifyService = notifyService;
    this.pwaContactService = pwaContactService;
    this.emailCaseLinkService = emailCaseLinkService;
  }


  public void withdrawApplication(WithdrawApplicationForm form,
                                  PwaApplication pwaApplication,
                                  AuthenticatedUserAccount withdrawingUser) {

    withdrawLastSubmittedDetail(pwaApplication, withdrawingUser.getLinkedPerson(), form.getWithdrawalReason());
    withdrawUpdateRequestedDetail(pwaApplication);

    camundaWorkflowService.deleteProcessInstanceAndThenTasks(pwaApplication);

    withdrawConsultationService.withdrawAllOpenConsultationRequests(pwaApplication, withdrawingUser);
    sendWithdrawalEmails(pwaApplication, withdrawingUser);
  }

  private void withdrawLastSubmittedDetail(PwaApplication pwaApplication, Person withdrawingPerson, String withdrawalReason) {

    pwaApplicationDetailService.doWithLastSubmittedDetailIfExists(pwaApplication, (lastSubmittedDetail) ->
        pwaApplicationDetailService.setWithdrawn(lastSubmittedDetail, withdrawingPerson, withdrawalReason));
  }

  private void withdrawUpdateRequestedDetail(PwaApplication pwaApplication) {

    Consumer<PwaApplicationDetail> endUpdateRequestDetailFunction = (updateRequestedDetail) -> {
      applicationUpdateRequestService.endUpdateRequestIfExists(updateRequestedDetail);
      var lastSubmittedDetail = pwaApplicationDetailService.getLatestSubmittedDetail(pwaApplication).orElseThrow(() ->
          new WithdrawApplicationException(
              "Last submitted detail could not be found for PwaApplication with id: " + pwaApplication.getId()));
      pwaApplicationDetailService.transferTipFlag(updateRequestedDetail, lastSubmittedDetail);
    };

    pwaApplicationDetailService.doWithCurrentUpdateRequestedDetailIfExists(pwaApplication, endUpdateRequestDetailFunction);
  }

  private void sendWithdrawalEmails(PwaApplication pwaApplication, AuthenticatedUserAccount withdrawingUser) {
    var emailRecipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplication,
        PwaContactRole.PREPARER
    );
    emailRecipients.forEach(recipient -> {
      var emailProps = new ApplicationWithdrawnEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          withdrawingUser.getFullName(),
          emailCaseLinkService.generateCaseManagementLink(pwaApplication)
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
