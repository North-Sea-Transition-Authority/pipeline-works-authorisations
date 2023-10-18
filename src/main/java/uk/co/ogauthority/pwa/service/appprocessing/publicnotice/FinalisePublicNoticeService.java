package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
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
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticePublicationEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticePublicationUpdateEmailProps;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDate;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDatesRepository;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PublicNoticeCaseOfficerReviewResult;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PublicNoticePublicationState;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
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
  private final CaseLinkService caseLinkService;
  private final Clock clock;


  @Autowired
  public FinalisePublicNoticeService(
      PublicNoticeService publicNoticeService,
      FinalisePublicNoticeValidator finalisePublicNoticeValidator,
      CamundaWorkflowService camundaWorkflowService,
      PublicNoticeDatesRepository publicNoticeDatesRepository,
      @Value("${service.full-name}") String serviceName,
      PwaContactService pwaContactService, NotifyService notifyService,
      CaseLinkService caseLinkService,
      @Qualifier("utcClock") Clock clock) {
    this.publicNoticeService = publicNoticeService;
    this.finalisePublicNoticeValidator = finalisePublicNoticeValidator;
    this.camundaWorkflowService = camundaWorkflowService;
    this.publicNoticeDatesRepository = publicNoticeDatesRepository;
    this.serviceName = serviceName;
    this.pwaContactService = pwaContactService;
    this.notifyService = notifyService;
    this.caseLinkService = caseLinkService;
    this.clock = clock;
  }




  public boolean publicNoticeCanBeFinalised(PwaApplication pwaApplication) {
    return publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)
        .stream()
        .anyMatch(publicNotice -> publicNotice.getPwaApplication().equals(pwaApplication));
  }

  public boolean publicNoticeDatesCanBeUpdated(PwaApplication pwaApplication) {
    var applicableStatuses = Set.of(PublicNoticeStatus.WAITING, PublicNoticeStatus.PUBLISHED);
    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    return publicNotice != null && applicableStatuses.contains(publicNotice.getStatus());
  }


  public BindingResult validate(FinalisePublicNoticeForm form, BindingResult bindingResult, PwaApplication pwaApplication) {
    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    var activePublicNoticeDate = getActivePublicNoticeDate(publicNotice);
    finalisePublicNoticeValidator.validate(
        form,
        bindingResult,
        activePublicNoticeDate.getPublicationStartTimestamp().isBefore(clock.instant()));
    return bindingResult;
  }

  public BindingResult validate(FinalisePublicNoticeForm form, BindingResult bindingResult) {
    finalisePublicNoticeValidator.validate(form, bindingResult);
    return bindingResult;
  }


  private void sendPublicationEmails(PwaApplication pwaApplication, LocalDate startDate) {

    var emailRecipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplication,
        PwaContactRole.PREPARER);
    var caseManagementLink = caseLinkService.generateCaseManagementLink(pwaApplication);

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
    completeTaskAndSavePublicNotice(publicNotice, existingStatus);
  }

  private void completeTaskAndSavePublicNotice(PublicNotice publicNotice,
                                               PublicNoticeStatus existingStatus) {

    camundaWorkflowService.setWorkflowProperty(publicNotice, PublicNoticeCaseOfficerReviewResult.WAIT_FOR_START_DATE);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(publicNotice, existingStatus.getWorkflowTask()));
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
    publicNotice.setStatus(PublicNoticeStatus.WAITING);
    completeTaskAndSavePublicNotice(publicNotice, PublicNoticeStatus.CASE_OFFICER_REVIEW);

    if (startDate.isBefore(LocalDate.now()) || startDate.isEqual(LocalDate.now())) {
      publishPublicNotice(publicNotice);
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
    var caseManagementLink = caseLinkService.generateCaseManagementLink(pwaApplication);

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
    newPublicNoticeDate.setDateChangeReason(form.getDateChangeReason());
    publicNoticeDatesRepository.save(newPublicNoticeDate);
    boolean currentPublishDateInFuture = activePublicNoticeDate.getPublicationStartTimestamp().isAfter(Instant.now());
    boolean newPublishDateInPast = startDate.isBefore(LocalDate.now()) || startDate.isEqual(LocalDate.now());
    if (newPublishDateInPast && currentPublishDateInFuture) {
      camundaWorkflowService.completeTask(new WorkflowTaskInstance(publicNotice, PwaApplicationPublicNoticeWorkflowTask.WAITING));
      publicNotice.setStatus(PublicNoticeStatus.PUBLISHED);
      publicNoticeService.savePublicNotice(publicNotice);
      sendPublicationUpdateEmails(pwaApplication, startDate);
      return;
    }

    boolean currentPublishDateInPast = activePublicNoticeDate.getPublicationStartTimestamp().isBefore(Instant.now());
    boolean newPublishDateInFuture = startDate.isAfter(LocalDate.now());
    if (currentPublishDateInPast && newPublishDateInFuture) {
      camundaWorkflowService.setWorkflowProperty(publicNotice, PublicNoticePublicationState.WAIT_FOR_START_DATE);
      camundaWorkflowService.completeTask(
          new WorkflowTaskInstance(publicNotice, PwaApplicationPublicNoticeWorkflowTask.PUBLISHED));
      publicNotice.setStatus(PublicNoticeStatus.WAITING);
      publicNoticeService.savePublicNotice(publicNotice);
      sendPublicationUpdateEmails(pwaApplication, startDate);
    }
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
