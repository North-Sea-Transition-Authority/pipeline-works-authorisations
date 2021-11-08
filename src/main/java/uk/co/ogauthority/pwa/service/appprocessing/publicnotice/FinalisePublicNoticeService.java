package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.email.EmailCaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticePublicationEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticePublicationUpdateEmailProps;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDate;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDatesRepository;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PublicNoticeCaseOfficerReviewResult;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
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
  private final Clock clock;


  @Autowired
  public FinalisePublicNoticeService(
      PublicNoticeService publicNoticeService,
      FinalisePublicNoticeValidator finalisePublicNoticeValidator,
      CamundaWorkflowService camundaWorkflowService,
      PublicNoticeDatesRepository publicNoticeDatesRepository,
      @Value("${service.name}") String serviceName,
      PwaContactService pwaContactService, NotifyService notifyService,
      EmailCaseLinkService emailCaseLinkService,
      @Qualifier("utcClock") Clock clock) {
    this.publicNoticeService = publicNoticeService;
    this.finalisePublicNoticeValidator = finalisePublicNoticeValidator;
    this.camundaWorkflowService = camundaWorkflowService;
    this.publicNoticeDatesRepository = publicNoticeDatesRepository;
    this.serviceName = serviceName;
    this.pwaContactService = pwaContactService;
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.clock = clock;
  }




  public boolean publicNoticeCanBeFinalised(PwaApplication pwaApplication) {
    return publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)
        .stream()
        .anyMatch(publicNotice -> publicNotice.getPwaApplication().equals(pwaApplication));
  }

  public boolean publicNoticeDatesCanBeUpdated(PwaApplication pwaApplication) {
    return publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.WAITING)
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


  public void publishPublicNotice(PublicNotice publicNotice) {

    var existingStatus = publicNotice.getStatus();
    publicNotice.setStatus(PublicNoticeStatus.PUBLISHED);
    camundaWorkflowService.setWorkflowProperty(publicNotice, PublicNoticeCaseOfficerReviewResult.PUBLICATION_STARTED);
    completeTaskAndSavePublicNotice(publicNotice, existingStatus);
  }

  private void completeTaskAndSavePublicNotice(PublicNotice publicNotice,
                                               PublicNoticeStatus existingStatus) {

    camundaWorkflowService.completeTask(new WorkflowTaskInstance(
        publicNotice, existingStatus.getWorkflowTask()));
    publicNoticeService.savePublicNotice(publicNotice);
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
        createPublicationEndDateInstant(startDate, form.getDaysToBePublishedFor()),
        authenticatedUserAccount.getLinkedPerson().getId().asInt(),
        clock.instant());
    publicNoticeDatesRepository.save(publicNoticeDate);

    if (startDate.isBefore(LocalDate.now()) || startDate.isEqual(LocalDate.now())) {
      publishPublicNotice(publicNotice);

    } else {
      publicNotice.setStatus(PublicNoticeStatus.WAITING);
      camundaWorkflowService.setWorkflowProperty(publicNotice, PublicNoticeCaseOfficerReviewResult.WAIT_FOR_START_DATE);
      completeTaskAndSavePublicNotice(publicNotice, PublicNoticeStatus.CASE_OFFICER_REVIEW);
    }

    sendPublicationEmails(publicNotice.getPwaApplication(), startDate);
  }

  private Instant createPublicationEndDateInstant(LocalDate startDate, int totalDaysToPublishFor) {
    return startDate.plusDays(totalDaysToPublishFor).atStartOfDay(ZoneId.systemDefault()).toInstant();
  }

  private PublicNoticeDate getActivePublicNoticeDate(PublicNotice publicNotice) {
    return publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publicNotice)
        .orElseThrow(() -> new EntityLatestVersionNotFoundException(String.format(
            "Couldn't find public notice date with public notice ID: %s", publicNotice.getId())));
  }


  private void sendPublicationUpdateEmails(PwaApplication pwaApplication, LocalDate startDate) {

    var emailRecipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplication,
        PwaContactRole.PREPARER);
    var caseManagementLink = emailCaseLinkService.generateCaseManagementLink(pwaApplication);

    emailRecipients.forEach(recipient -> {
      var emailProps = new PublicNoticePublicationUpdateEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          caseManagementLink,
          DateUtils.formatDate(startDate));
      notifyService.sendEmail(emailProps, recipient.getEmailAddress());
    });
  }

  @Transactional
  public void updatePublicNoticeDate(PwaApplication pwaApplication,
                                     FinalisePublicNoticeForm form,
                                     AuthenticatedUserAccount authenticatedUserAccount) {

    var time = clock.instant();
    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    var activePublicNoticeDate = getActivePublicNoticeDate(publicNotice);

    activePublicNoticeDate.setEndedByPersonId(authenticatedUserAccount.getLinkedPerson().getId().asInt());
    activePublicNoticeDate.setEndedTimestamp(time);
    publicNoticeDatesRepository.save(activePublicNoticeDate);

    var startDate = LocalDate.of(form.getStartYear(), form.getStartMonth(), form.getStartDay());
    var newPublicNoticeDate = new PublicNoticeDate(
        publicNotice,
        startDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        createPublicationEndDateInstant(startDate, form.getDaysToBePublishedFor()),
        authenticatedUserAccount.getLinkedPerson().getId().asInt(),
        time);
    publicNoticeDatesRepository.save(newPublicNoticeDate);

    if (startDate.isBefore(LocalDate.now()) || startDate.isEqual(LocalDate.now())) {
      camundaWorkflowService.setWorkflowProperty(publicNotice, PublicNoticeCaseOfficerReviewResult.PUBLICATION_STARTED);
      camundaWorkflowService.completeTask(new WorkflowTaskInstance(
          publicNotice, PwaApplicationPublicNoticeWorkflowTask.WAITING));
      publicNotice.setStatus(PublicNoticeStatus.PUBLISHED);
      publicNoticeService.savePublicNotice(publicNotice);
    }

    sendPublicationUpdateEmails(pwaApplication, startDate);
  }


  public void mapUnpublishedPublicNoticeDateToForm(PwaApplication pwaApplication,
                                                   FinalisePublicNoticeForm form) {

    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    var activePublicNoticeDate = getActivePublicNoticeDate(publicNotice);

    DateUtils.setYearMonthDayFromInstant(
        form::setStartYear,
        form::setStartMonth,
        form::setStartDay,
        activePublicNoticeDate.getPublicationStartTimestamp()
    );

    form.setDaysToBePublishedFor((int) activePublicNoticeDate.getPublicationDaysLength());
  }
}
