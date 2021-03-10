package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Set;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.publicnotice.WithdrawPublicNoticeForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticeWithdrawnEmailProps;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.validators.publicnotice.WithdrawPublicNoticeValidator;

@Service
public class WithdrawPublicNoticeService {


  private final PublicNoticeService publicNoticeService;
  private final WithdrawPublicNoticeValidator withdrawPublicNoticeValidator;
  private final CamundaWorkflowService camundaWorkflowService;
  private final PwaTeamService pwaTeamService;
  private final PwaContactService pwaContactService;
  private final NotifyService notifyService;
  private final Clock clock;


  @Autowired
  public WithdrawPublicNoticeService(
      PublicNoticeService publicNoticeService,
      WithdrawPublicNoticeValidator withdrawPublicNoticeValidator,
      CamundaWorkflowService camundaWorkflowService,
      PwaTeamService pwaTeamService, PwaContactService pwaContactService,
      NotifyService notifyService,
      @Qualifier("utcClock") Clock clock) {
    this.publicNoticeService = publicNoticeService;
    this.withdrawPublicNoticeValidator = withdrawPublicNoticeValidator;
    this.camundaWorkflowService = camundaWorkflowService;
    this.pwaTeamService = pwaTeamService;
    this.pwaContactService = pwaContactService;
    this.notifyService = notifyService;
    this.clock = clock;
  }




  public boolean publicNoticeCanBeWithdrawn(PwaApplication pwaApplication) {
    return publicNoticeService.getOpenPublicNotices()
        .stream()
        .anyMatch(publicNotice -> publicNotice.getPwaApplication().equals(pwaApplication));
  }


  public BindingResult validate(WithdrawPublicNoticeForm form, BindingResult bindingResult) {
    withdrawPublicNoticeValidator.validate(form, bindingResult);
    return bindingResult;
  }



  @Transactional
  public void withdrawPublicNotice(PwaApplication pwaApplication,
                                   WithdrawPublicNoticeForm form,
                                   AuthenticatedUserAccount authenticatedUserAccount) {

    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    var publicNoticeStatusBeforeWithdrawal = publicNotice.getStatus();

    var workflowTaskInstance = new WorkflowTaskInstance(publicNotice, publicNoticeStatusBeforeWithdrawal.getWorkflowTask());
    camundaWorkflowService.deleteProcessAndTask(workflowTaskInstance);

    publicNotice.setStatus(PublicNoticeStatus.WITHDRAWN);
    publicNotice.setWithdrawalReason(form.getWithdrawalReason());
    publicNotice.setWithdrawalTimestamp(clock.instant());
    publicNotice.setWithdrawingPersonId(authenticatedUserAccount.getLinkedPerson().getId());
    publicNoticeService.savePublicNotice(publicNotice);

    var emailRecipients = new ArrayList<Person>();
    var statusesDeterminingPublicNoticeWasSentToApplicant = Set.of(
        PublicNoticeStatus.APPLICANT_UPDATE, PublicNoticeStatus.CASE_OFFICER_REVIEW, PublicNoticeStatus.FINALISATION);

    if (statusesDeterminingPublicNoticeWasSentToApplicant.contains(publicNoticeStatusBeforeWithdrawal)) {
      emailRecipients.addAll(pwaContactService.getPeopleInRoleForPwaApplication(
          pwaApplication,
          PwaContactRole.PREPARER
      ));
    }

    emailRecipients.addAll(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER));
    emailRecipients.forEach(recipient -> {

      var withdrawnEmailProps = new PublicNoticeWithdrawnEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          authenticatedUserAccount.getLinkedPerson().getFullName(),
          form.getWithdrawalReason());

      notifyService.sendEmail(withdrawnEmailProps, recipient.getEmailAddress());
    });

  }


}
