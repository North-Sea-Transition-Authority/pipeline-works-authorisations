package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import com.google.common.annotations.VisibleForTesting;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.TemplateTextType;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDate;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocumentLink;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticeApprovalRequestEmailProps;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.view.publicnotice.AllPublicNoticesView;
import uk.co.ogauthority.pwa.model.view.publicnotice.PublicNoticeView;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDatesRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.template.TemplateTextService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeDraftValidator;

@Service
public class PublicNoticeService implements AppProcessingService {

  private final TemplateTextService templateTextService;
  private final PublicNoticeDraftValidator publicNoticeDraftValidator;
  private final AppFileService appFileService;
  private final PublicNoticeRepository publicNoticeRepository;
  private final PublicNoticeRequestRepository publicNoticeRequestRepository;
  private final PublicNoticeDocumentRepository publicNoticeDocumentRepository;
  private final PublicNoticeDocumentLinkRepository publicNoticeDocumentLinkRepository;
  private final PublicNoticeDatesRepository publicNoticeDatesRepository;
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;
  private final NotifyService notifyService;
  private final EmailCaseLinkService emailCaseLinkService;
  private final PwaTeamService pwaTeamService;
  private final PersonService personService;

  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.PUBLIC_NOTICE;
  private static final Set<PublicNoticeStatus> ENDED_STATUSES = Set.of(PublicNoticeStatus.ENDED, PublicNoticeStatus.WITHDRAWN);

  @Autowired
  public PublicNoticeService(
      TemplateTextService templateTextService,
      PublicNoticeDraftValidator publicNoticeDraftValidator,
      AppFileService appFileService,
      PublicNoticeRepository publicNoticeRepository,
      PublicNoticeRequestRepository publicNoticeRequestRepository,
      PublicNoticeDocumentRepository publicNoticeDocumentRepository,
      PublicNoticeDocumentLinkRepository publicNoticeDocumentLinkRepository,
      PublicNoticeDatesRepository publicNoticeDatesRepository,
      CamundaWorkflowService camundaWorkflowService,
      @Qualifier("utcClock") Clock clock, NotifyService notifyService,
      EmailCaseLinkService emailCaseLinkService,
      PwaTeamService pwaTeamService,
      PersonService personService) {
    this.templateTextService = templateTextService;
    this.publicNoticeDraftValidator = publicNoticeDraftValidator;
    this.appFileService = appFileService;
    this.publicNoticeRepository = publicNoticeRepository;
    this.publicNoticeRequestRepository = publicNoticeRequestRepository;
    this.publicNoticeDocumentRepository = publicNoticeDocumentRepository;
    this.publicNoticeDocumentLinkRepository = publicNoticeDocumentLinkRepository;
    this.publicNoticeDatesRepository = publicNoticeDatesRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.clock = clock;
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.pwaTeamService = pwaTeamService;
    this.personService = personService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE);
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    boolean atLeastOneSatisfactoryVersion = processingContext.getApplicationInvolvement().hasAtLeastOneSatisfactoryVersion();

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        atLeastOneSatisfactoryVersion ? TaskTag.from(TaskStatus.NOT_STARTED) : TaskTag.from(TaskStatus.CANNOT_START_YET),
        atLeastOneSatisfactoryVersion ? TaskState.EDIT : TaskState.LOCK,
        task.getDisplayOrder());

  }

  public List<PublicNotice> getPublicNoticesByStatus(PublicNoticeStatus publicNoticeStatus) {
    return publicNoticeRepository.findAllByStatus(publicNoticeStatus);
  }

  public List<PublicNotice> getOpenPublicNotices() {
    return publicNoticeRepository.findAllByStatusNotIn(ENDED_STATUSES);
  }

  public PublicNotice getLatestPublicNotice(PwaApplication pwaApplication) {
    return publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)
        .orElseThrow(() -> new EntityLatestVersionNotFoundException(String.format(
        "Couldn't find public notice with pwaApplication ID: %s", pwaApplication.getId())));
  }

  public PublicNoticeRequest getLatestPublicNoticeRequest(PublicNotice publicNotice) {
    return publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(publicNotice)
        .orElseThrow(() -> new EntityLatestVersionNotFoundException(String.format(
            "Couldn't find public notice request with public notice ID: %s", publicNotice.getId())));
  }

  public PublicNoticeDate getLatestPublicNoticeDate(PublicNotice publicNotice) {
    return publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publicNotice)
        .orElseThrow(() -> new EntityLatestVersionNotFoundException(String.format(
            "Couldn't find public notice date with public notice ID: %s", publicNotice.getId())));
  }

  public void savePublicNoticeRequest(PublicNoticeRequest publicNoticeRequest) {
    publicNoticeRequestRepository.save(publicNoticeRequest);
  }

  public PublicNotice savePublicNotice(PublicNotice publicNotice) {
    return publicNoticeRepository.save(publicNotice);
  }

  void archivePublicNoticeDocument(PublicNoticeDocument publicNoticeDocument) {
    publicNoticeDocument.setDocumentType(PublicNoticeDocumentType.ARCHIVED);
    publicNoticeDocumentRepository.save(publicNoticeDocument);
  }

  private Integer getPublicNoticeLatestVersionNumber(PwaApplication pwaApplication) {
    var latestPublicNoticeOpt = publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication);
    return latestPublicNoticeOpt.isPresent() ? latestPublicNoticeOpt.get().getVersion() : 0;
  }

  public void mapPublicNoticeDraftToForm(PwaApplication pwaApplication, PublicNoticeDraftForm form) {

    var coverLetterText = templateTextService.getLatestVersionTextByType(TemplateTextType.PUBLIC_NOTICE_COVER_LETTER);
    var draftPublicNoticeOpt = publicNoticeRepository.findByStatusAndPwaApplication(PublicNoticeStatus.DRAFT, pwaApplication);

    if (draftPublicNoticeOpt.isPresent()) {
      var publicNoticeRequest = getLatestPublicNoticeRequest(draftPublicNoticeOpt.get());
      coverLetterText = publicNoticeRequest.getCoverLetterText();
      form.setReason(publicNoticeRequest.getReason());
      if (PublicNoticeRequestReason.CONSULTEES_NOT_ALL_CONTENT.equals(form.getReason())) {
        form.setReasonDescription(publicNoticeRequest.getReasonDescription());
      }
      appFileService.mapFilesToForm(form, pwaApplication, FILE_PURPOSE);
    }

    form.setCoverLetterText(coverLetterText);
  }


  private PublicNoticeRequest createInitialPublicNoticeRequestFromForm(PublicNoticeDraftForm form,
                                                                       PublicNotice publicNotice,
                                                                       Person person) {
    var publicNoticeRequest = new PublicNoticeRequest();
    publicNoticeRequest.setPublicNotice(publicNotice);
    publicNoticeRequest.setCoverLetterText(form.getCoverLetterText());
    publicNoticeRequest.setStatus(PublicNoticeRequestStatus.WAITING_MANAGER_APPROVAL);
    publicNoticeRequest.setReason(form.getReason());
    publicNoticeRequest.setReasonDescription(
        PublicNoticeRequestReason.CONSULTEES_NOT_ALL_CONTENT.equals(form.getReason()) ? form.getReasonDescription() : null);
    publicNoticeRequest.setVersion(1);
    publicNoticeRequest.setSubmittedTimestamp(Instant.now(clock));
    publicNoticeRequest.setCreatedByPersonId(person.getId().asInt());
    return publicNoticeRequest;
  }


  private void sendPublicNoticeApprovalEmails(PwaApplication pwaApplication, String publicNoticeReason) {

    var caseManagementLink = emailCaseLinkService.generateCaseManagementLink(pwaApplication);

    var pwaManagers = pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER);

    pwaManagers.forEach(pwaManager -> {

      var emailProps = new PublicNoticeApprovalRequestEmailProps(
          pwaManager.getFullName(),
          pwaApplication.getAppReference(),
          publicNoticeReason,
          caseManagementLink);
      notifyService.sendEmail(emailProps, pwaManager.getEmailAddress());
    });
  }

  @Transactional
  public void createPublicNoticeAndStartWorkflow(PublicNoticeDraftForm form,
                                                 PwaApplication pwaApplication,
                                                 AuthenticatedUserAccount userAccount) {

    var publicNotice = new PublicNotice(
        pwaApplication, PublicNoticeStatus.MANAGER_APPROVAL, getPublicNoticeLatestVersionNumber(pwaApplication) + 1);
    publicNotice = publicNoticeRepository.save(publicNotice);

    appFileService.updateFiles(form, pwaApplication, FILE_PURPOSE, FileUpdateMode.KEEP_UNLINKED_FILES, userAccount);

    var publicNoticeDocVersion = 1;
    var publicNoticeDocument = new PublicNoticeDocument(
        publicNotice, publicNoticeDocVersion, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT);
    publicNoticeDocument = publicNoticeDocumentRepository.save(publicNoticeDocument);

    //accessing list at index 0 as validator ensures there is always and only 1 file
    var publicNoticeDocumentLink = createPublicNoticeDocumentLinkFromForm(
        pwaApplication, form.getUploadedFileWithDescriptionForms().get(0), publicNoticeDocument);
    publicNoticeDocumentLinkRepository.save(publicNoticeDocumentLink);

    var publicNoticeRequest = createInitialPublicNoticeRequestFromForm(form, publicNotice, userAccount.getLinkedPerson());
    publicNoticeRequestRepository.save(publicNoticeRequest);

    camundaWorkflowService.startWorkflow(publicNotice);
    sendPublicNoticeApprovalEmails(pwaApplication, form.getReason().getReasonText());
  }

  public PublicNoticeDocumentLink createPublicNoticeDocumentLinkFromForm(PwaApplication pwaApplication,
                                                                         UploadFileWithDescriptionForm form,
                                                                         PublicNoticeDocument publicNoticeDocument) {
    var documentUploadedFileId = form.getUploadedFileId();
    var docAppFile = appFileService.getAppFileByPwaApplicationAndFileId(pwaApplication, documentUploadedFileId);
    return new PublicNoticeDocumentLink(publicNoticeDocument, docAppFile);
  }

  public boolean canCreatePublicNoticeDraft(PwaApplication pwaApplication) {
    var latestPublicNoticeOptional = publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication);
    return latestPublicNoticeOptional.map(publicNotice -> canCreatePublicNoticeDraft(publicNotice.getStatus()))
        .orElse(true);
  }

  private boolean canCreatePublicNoticeDraft(PublicNoticeStatus publicNoticeStatus) {
    return ENDED_STATUSES.contains(publicNoticeStatus);
  }

  private PublicNoticeView createViewFromPublicNotice(PublicNotice publicNotice) {

    var publicNoticeRequest = getLatestPublicNoticeRequest(publicNotice);
    var latestDocumentComments = publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(
        publicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT);
    String withdrawingPersonName = null;
    String withdrawnTimestamp = null;
    String publicationStartTimestamp = null;
    String publicationEndTimestamp = null;


    if (publicNotice.getStatus().equals(PublicNoticeStatus.WITHDRAWN)) {
      withdrawingPersonName = personService.getPersonById(publicNotice.getWithdrawingPersonId()).getFullName();
      withdrawnTimestamp = DateUtils.formatDate(publicNotice.getWithdrawalTimestamp());
    }

    var publicNoticeDateOpt = publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publicNotice);
    if (publicNoticeDateOpt.isPresent()) {
      var publicNoticeDate = publicNoticeDateOpt.get();
      publicationStartTimestamp = DateUtils.formatDate(publicNoticeDate.getPublicationStartTimestamp());
      publicationEndTimestamp = DateUtils.formatDate(publicNoticeDate.getPublicationEndTimestamp());
    }

    return new PublicNoticeView(
        publicNotice.getStatus(),
        DateUtils.formatDate(publicNoticeRequest.getSubmittedTimestamp()),
        latestDocumentComments.map(PublicNoticeDocument::getComments).orElse(null),
        withdrawingPersonName,
        withdrawnTimestamp,
        publicationStartTimestamp,
        publicationEndTimestamp
    );
  }

  @VisibleForTesting
  Set<PublicNoticeAction> getAvailablePublicNoticeActions(PublicNoticeStatus publicNoticeStatus,
                                                          PwaAppProcessingContext pwaAppProcessingContext) {

    var processingPermissions = pwaAppProcessingContext.getAppProcessingPermissions();
    Set<PublicNoticeAction> publicNoticeActions = new HashSet<>();

    if (processingPermissions.contains(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)
        && (publicNoticeStatus == null || canCreatePublicNoticeDraft(publicNoticeStatus))) {
      return Set.of(PublicNoticeAction.NEW_DRAFT);
    }

    if (processingPermissions.contains(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)
        && PublicNoticeStatus.DRAFT.equals(publicNoticeStatus)) {
      publicNoticeActions.add(PublicNoticeAction.UPDATE_DRAFT);

    } else if (processingPermissions.contains(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE)
        && PublicNoticeStatus.MANAGER_APPROVAL.equals(publicNoticeStatus)) {
      publicNoticeActions.add(PublicNoticeAction.APPROVE);

    } else if (processingPermissions.contains(PwaAppProcessingPermission.REQUEST_PUBLIC_NOTICE_UPDATE)
        && PublicNoticeStatus.CASE_OFFICER_REVIEW.equals(publicNoticeStatus)) {
      publicNoticeActions.add(PublicNoticeAction.REQUEST_DOCUMENT_UPDATE);
    }

    if (processingPermissions.contains(PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE)
        && PublicNoticeStatus.CASE_OFFICER_REVIEW.equals(publicNoticeStatus)) {
      publicNoticeActions.add(PublicNoticeAction.FINALISE);
    }

    if (processingPermissions.contains(PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE)
        && PublicNoticeStatus.WAITING.equals(publicNoticeStatus)) {
      publicNoticeActions.add(PublicNoticeAction.UPDATE_DATES);
    }

    if (processingPermissions.contains(PwaAppProcessingPermission.WITHDRAW_PUBLIC_NOTICE)
        && publicNoticeStatus != null && !ENDED_STATUSES.contains(publicNoticeStatus)) {
      publicNoticeActions.add(PublicNoticeAction.WITHDRAW);
    }

    return publicNoticeActions;
  }


  public AllPublicNoticesView getAllPublicNoticeViews(PwaAppProcessingContext pwaAppProcessingContext) {

    var publicNotices = publicNoticeRepository.findAllByPwaApplicationOrderByVersionDesc(pwaAppProcessingContext.getPwaApplication());
    PublicNoticeView currentPublicNotice = null;
    List<PublicNoticeView> historicalPublicNotices = new ArrayList<>();
    Set<PublicNoticeAction> availableActions = getAvailablePublicNoticeActions(null, pwaAppProcessingContext);

    for (var x = 0; x < publicNotices.size(); x++) {
      //public notices are ordered with the newest at first index so just check if active
      if (x == 0 && !ENDED_STATUSES.contains(publicNotices.get(x).getStatus())) {
        currentPublicNotice = createViewFromPublicNotice(publicNotices.get(x));
        availableActions = getAvailablePublicNoticeActions(publicNotices.get(x).getStatus(), pwaAppProcessingContext);

      } else {
        historicalPublicNotices.add(createViewFromPublicNotice(publicNotices.get(x)));
      }
    }

    return new AllPublicNoticesView(currentPublicNotice, historicalPublicNotices, availableActions);
  }

  public PublicNoticeDocument getLatestPublicNoticeDocument(PublicNotice publicNotice) {
    return publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(publicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT)
        .orElseThrow(() -> new EntityLatestVersionNotFoundException(String.format(
            "Couldn't find public notice document with public notice ID: %s", publicNotice.getId())));
  }

  public UploadedFileView getLatestPublicNoticeDocumentFileView(PwaApplication pwaApplication) {

    var publicNotice = getLatestPublicNotice(pwaApplication);
    var latestPublicNoticeDocument = getLatestPublicNoticeDocument(publicNotice);
    var documentLink = publicNoticeDocumentLinkRepository.findByPublicNoticeDocument(latestPublicNoticeDocument)
        .orElseThrow(() -> new EntityLatestVersionNotFoundException(String.format(
            "Couldn't find public notice document link with public notice document ID: %s", latestPublicNoticeDocument.getId())));

    return appFileService.getUploadedFileView(
        pwaApplication, documentLink.getAppFile().getFileId(), FILE_PURPOSE, ApplicationFileLinkStatus.FULL);
  }


  public BindingResult validate(PublicNoticeDraftForm form, BindingResult bindingResult) {
    publicNoticeDraftValidator.validate(form, bindingResult);
    return bindingResult;
  }


  public Optional<PublicNoticeDocumentLink> getPublicNoticeDocumentLink(AppFile appFile) {
    return publicNoticeDocumentLinkRepository.findByAppFile(appFile);
  }

  @Transactional
  public void deleteFileLinkAndPublicNoticeDocument(PublicNoticeDocumentLink publicNoticeDocumentLink) {
    publicNoticeDocumentLinkRepository.delete(publicNoticeDocumentLink);
    publicNoticeDocumentRepository.delete(publicNoticeDocumentLink.getPublicNoticeDocument());
  }

  public boolean publicNoticeInProgress(PwaApplication pwaApplication) {
    return publicNoticeRepository.findAllByPwaApplication(pwaApplication).stream()
        .anyMatch(notice -> !ENDED_STATUSES.contains(notice.getStatus()));
  }

}
