package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.email.EmailRecipientWithName;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticeWithdrawnEmailProps;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.form.publicnotice.WithdrawPublicNoticeForm;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.validators.publicnotice.WithdrawPublicNoticeValidator;

@Service
public class WithdrawPublicNoticeService {


  private final PublicNoticeService publicNoticeService;
  private final WithdrawPublicNoticeValidator withdrawPublicNoticeValidator;
  private final CamundaWorkflowService camundaWorkflowService;
  private final PwaTeamService pwaTeamService;
  private final PwaContactService pwaContactService;
  private final Clock clock;
  private final EmailService emailService;


  @Autowired
  public WithdrawPublicNoticeService(
      PublicNoticeService publicNoticeService,
      WithdrawPublicNoticeValidator withdrawPublicNoticeValidator,
      CamundaWorkflowService camundaWorkflowService,
      PwaTeamService pwaTeamService,
      PwaContactService pwaContactService,
      @Qualifier("utcClock") Clock clock,
      EmailService emailService) {
    this.publicNoticeService = publicNoticeService;
    this.withdrawPublicNoticeValidator = withdrawPublicNoticeValidator;
    this.camundaWorkflowService = camundaWorkflowService;
    this.pwaTeamService = pwaTeamService;
    this.pwaContactService = pwaContactService;
    this.clock = clock;
    this.emailService = emailService;
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

    if (!publicNotice.getStatus().equals(PublicNoticeStatus.PUBLISHED)) {
      var workflowTaskInstance = new WorkflowTaskInstance(publicNotice, publicNoticeStatusBeforeWithdrawal.getWorkflowTask());
      camundaWorkflowService.deleteProcessAndTask(workflowTaskInstance);
    }

    publicNotice.setStatus(PublicNoticeStatus.WITHDRAWN);
    publicNotice.setWithdrawalReason(form.getWithdrawalReason());
    publicNotice.setWithdrawalTimestamp(clock.instant());
    publicNotice.setWithdrawingPersonId(authenticatedUserAccount.getLinkedPerson().getId());
    publicNoticeService.savePublicNotice(publicNotice);

    try {
      var latestPublicNoticeDocument = publicNoticeService.getLatestPublicNoticeDocument(publicNotice);
      publicNoticeService.archivePublicNoticeDocument(latestPublicNoticeDocument);
    } catch (EntityLatestVersionNotFoundException e) {
      // do nothing if there's no doc attached to the PN, no work to do
    }

    var emailRecipients = new ArrayList<EmailRecipientWithName>();
    var statusesDeterminingPublicNoticeWasSentToApplicant = Set.of(
        PublicNoticeStatus.APPLICANT_UPDATE, PublicNoticeStatus.CASE_OFFICER_REVIEW, PublicNoticeStatus.WAITING);

    if (statusesDeterminingPublicNoticeWasSentToApplicant.contains(publicNoticeStatusBeforeWithdrawal)) {
      var recipients = pwaContactService.getPeopleInRoleForPwaApplication(
              pwaApplication,
              PwaContactRole.PREPARER
          )
          .stream()
          .map(EmailRecipientWithName::from)
          .collect(Collectors.toSet());

      emailRecipients.addAll(recipients);
    }

    Set<EmailRecipientWithName> recipientsWithRegulatorRole = pwaTeamService.getMembersWithRegulatorRole(Role.PWA_MANAGER)
        .stream()
        .map(EmailRecipientWithName::from)
        .collect(Collectors.toSet());

    emailRecipients.addAll(recipientsWithRegulatorRole);
    emailRecipients.forEach(recipient -> {

      var withdrawnEmailProps = new PublicNoticeWithdrawnEmailProps(
          recipient.fullName(),
          pwaApplication.getAppReference(),
          authenticatedUserAccount.getLinkedPerson().getFullName(),
          form.getWithdrawalReason());
      emailService.sendEmail(withdrawnEmailProps, recipient, pwaApplication.getAppReference());
    });

  }

}
