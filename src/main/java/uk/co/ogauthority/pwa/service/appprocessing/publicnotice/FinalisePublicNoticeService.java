package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import java.time.LocalDate;
import java.time.ZoneId;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDate;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticePublicationEmailProps;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDatesRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationPublicNoticeFinalisationResult;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.publicnotice.FinalisePublicNoticeValidator;

@Service
public class FinalisePublicNoticeService {


  private final PublicNoticeService publicNoticeService;
  private final FinalisePublicNoticeValidator finalisePublicNoticeValidator;
  private final CamundaWorkflowService camundaWorkflowService;
  private final PublicNoticeDatesRepository publicNoticeDatesRepository;
  private final String serviceName;
  private final PwaContactService pwaContactService;
  private final NotifyService notifyService;
  private final EmailCaseLinkService emailCaseLinkService;


  @Autowired
  public FinalisePublicNoticeService(
      PublicNoticeService publicNoticeService,
      FinalisePublicNoticeValidator finalisePublicNoticeValidator,
      CamundaWorkflowService camundaWorkflowService,
      PublicNoticeDatesRepository publicNoticeDatesRepository,
      @Value("${service.name}") String serviceName,
      PwaContactService pwaContactService, NotifyService notifyService,
      EmailCaseLinkService emailCaseLinkService) {
    this.publicNoticeService = publicNoticeService;
    this.finalisePublicNoticeValidator = finalisePublicNoticeValidator;
    this.camundaWorkflowService = camundaWorkflowService;
    this.publicNoticeDatesRepository = publicNoticeDatesRepository;
    this.serviceName = serviceName;
    this.pwaContactService = pwaContactService;
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
  }




  public boolean publicNoticeCanBeFinalised(PwaApplication pwaApplication) {
    return publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)
        .stream()
        .anyMatch(publicNotice -> publicNotice.getPwaApplication().equals(pwaApplication));
  }


  public BindingResult validate(FinalisePublicNoticeForm form, BindingResult bindingResult) {
    finalisePublicNoticeValidator.validate(form, bindingResult);
    return bindingResult;
  }


  private void sendPublicationEmails(PwaApplication pwaApplication, LocalDate startDate) {

    var emailRecipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplication,
        PwaContactRole.PREPARER);
    var caseManagementLink = emailCaseLinkService.generateCaseManagementLink(pwaApplication);

    emailRecipients.forEach(recipient -> {
      var emailProps = new PublicNoticePublicationEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          caseManagementLink,
          DateUtils.formatDate(startDate),
          serviceName);
      notifyService.sendEmail(emailProps, recipient.getEmailAddress());
    });
  }


  @Transactional
  public void finalisePublicNotice(PwaApplication pwaApplication,
                                   FinalisePublicNoticeForm form,
                                   AuthenticatedUserAccount authenticatedUserAccount) {

    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    var startDate = LocalDate.of(form.getStartYear(), form.getStartMonth(), form.getStartDay());

    var publicNoticeDate = new PublicNoticeDate(
        publicNotice,
        startDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        startDate.plusDays(form.getDaysToBePublishedFor()).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        authenticatedUserAccount.getLinkedPerson().getId().asInt());
    publicNoticeDatesRepository.save(publicNoticeDate);

    if (startDate.isBefore(LocalDate.now()) || startDate.isEqual(LocalDate.now())) {
      publicNotice.setStatus(PublicNoticeStatus.PUBLISHED);
      camundaWorkflowService.setWorkflowProperty(publicNotice, PwaApplicationPublicNoticeFinalisationResult.PUBLICATION_STARTED);

    } else {
      publicNotice.setStatus(PublicNoticeStatus.WAITING);
      camundaWorkflowService.setWorkflowProperty(publicNotice, PwaApplicationPublicNoticeFinalisationResult.WAIT_FOR_START_DATE);
    }

    camundaWorkflowService.completeTask(new WorkflowTaskInstance(
        publicNotice, PwaApplicationPublicNoticeWorkflowTask.CASE_OFFICER_REVIEW));
    publicNoticeService.savePublicNotice(publicNotice);
    sendPublicationEmails(pwaApplication, startDate);
  }


}
