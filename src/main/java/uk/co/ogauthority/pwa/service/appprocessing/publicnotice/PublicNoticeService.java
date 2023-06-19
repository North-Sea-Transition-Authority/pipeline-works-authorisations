package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.controller.publicnotice.PublicNoticeDraftController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.AppProcessingService;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.TemplateTextType;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDate;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocumentLink;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeRequest;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.model.view.publicnotice.AllPublicNoticesView;
import uk.co.ogauthority.pwa.model.view.publicnotice.PublicNoticeView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDatesRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeRequestRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.template.TemplateTextService;
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
  private final PersonService personService;

  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.PUBLIC_NOTICE;
  private static final Set<PublicNoticeStatus> ENDED_STATUSES = Set.of(PublicNoticeStatus.ENDED, PublicNoticeStatus.WITHDRAWN);
  private static final Set<PublicNoticeStatus> APPLICANT_VIEW_STATUSES = Set.of(
      PublicNoticeStatus.WAITING, PublicNoticeStatus.PUBLISHED, PublicNoticeStatus.ENDED);
  private static final Set<PwaApplicationType> PUBLIC_NOTICE_APP_TYPES = EnumSet.of(
      PwaApplicationType.INITIAL, PwaApplicationType.CAT_1_VARIATION
  );

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
      PersonService personService) {
    this.templateTextService = templateTextService;
    this.publicNoticeDraftValidator = publicNoticeDraftValidator;
    this.appFileService = appFileService;
    this.publicNoticeRepository = publicNoticeRepository;
    this.publicNoticeRequestRepository = publicNoticeRequestRepository;
    this.publicNoticeDocumentRepository = publicNoticeDocumentRepository;
    this.publicNoticeDocumentLinkRepository = publicNoticeDocumentLinkRepository;
    this.publicNoticeDatesRepository = publicNoticeDatesRepository;
    this.personService = personService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return (
        processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)
            || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY)
            || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE)
            || (processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY))
            || (processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES))
      )
        && PUBLIC_NOTICE_APP_TYPES.contains(processingContext.getApplicationType());
  }



  private TaskStatus getPublicNoticeTaskStatus(PwaAppProcessingContext processingContext, boolean atLeastOneSatisfactoryVersion) {

    var latestPublicNoticeOpt = publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(processingContext.getPwaApplication());
    var permissions = processingContext.getAppProcessingPermissions();

    if (latestPublicNoticeOpt.isPresent() && permissions.contains(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE)
        && PublicNoticeStatus.MANAGER_APPROVAL.equals(latestPublicNoticeOpt.get().getStatus())) {
      return TaskStatus.ACTION_REQUIRED;

    } else if (latestPublicNoticeOpt.isPresent() && isPublicNoticeStatusEnded(latestPublicNoticeOpt.get().getStatus())) {
      return TaskStatus.COMPLETED;

    } else if (latestPublicNoticeOpt.isPresent()) {
      return TaskStatus.IN_PROGRESS;

    } else if (atLeastOneSatisfactoryVersion) {
      return TaskStatus.NOT_STARTED;

    } else {
      return TaskStatus.CANNOT_START_YET;
    }
  }

  public boolean publicNoticeTaskStarted(PwaApplication pwaApplication) {
    return !publicNoticeRepository.findAllByPwaApplication(pwaApplication).isEmpty();
  }

  public boolean publicNoticeTaskRequired(PwaApplication pwaApplication) {
    return PUBLIC_NOTICE_APP_TYPES.contains(pwaApplication.getApplicationType());
  }


  /** Task state is either editable or viewable depending on app state and app permissions for oga users.
   * Or it will be always locked for industry users
   */
  private TaskState getTaskState(PwaAppProcessingContext processingContext) {

    boolean atLeastOneSatisfactoryVersion = processingContext.getApplicationInvolvement().hasAtLeastOneSatisfactoryVersion();
    var permissions = processingContext.getAppProcessingPermissions();
    var appStatusesForViewing = Set.of(
        PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW, PwaApplicationStatus.COMPLETE);

    var taskState = permissions.contains(PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES)
        && appStatusesForViewing.contains(processingContext.getApplicationDetail().getStatus())
        && atLeastOneSatisfactoryVersion ? TaskState.VIEW : TaskState.LOCK;

    if (atLeastOneSatisfactoryVersion && permissions.contains(PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE)
        && processingContext.getApplicationDetail().getStatus().equals(PwaApplicationStatus.CASE_OFFICER_REVIEW)) {
      taskState = TaskState.EDIT;
    }

    return taskState;
  }


  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    boolean atLeastOneSatisfactoryVersion = processingContext.getApplicationInvolvement().hasAtLeastOneSatisfactoryVersion();

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        TaskTag.from(getPublicNoticeTaskStatus(processingContext, atLeastOneSatisfactoryVersion)),
        getTaskState(processingContext),
        task.getDisplayOrder());
  }

  public List<PublicNotice> getPublicNoticesByStatus(PublicNoticeStatus publicNoticeStatus) {
    return publicNoticeRepository.findAllByStatus(publicNoticeStatus);
  }

  public Optional<PublicNotice> getDraftPublicNoticeForApp(PwaApplication pwaApplication) {
    return publicNoticeRepository.findByStatusAndPwaApplication(PublicNoticeStatus.DRAFT, pwaApplication);
  }

  public List<PublicNotice> getOpenPublicNotices() {
    return publicNoticeRepository.findAllByStatusNotIn(ENDED_STATUSES);
  }

  public PublicNotice getLatestPublicNotice(PwaApplication pwaApplication) {
    return publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)
        .orElseThrow(() -> new EntityLatestVersionNotFoundException(String.format(
        "Couldn't find public notice with pwaApplication ID: %s", pwaApplication.getId())));
  }

  public Optional<PublicNotice> getLatestPublicNoticeOpt(PwaApplication pwaApplication) {
    return publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication);
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

  public List<PublicNotice> getAllPublicNoticesDueForPublishing() {

    var waitingPublicNotices = getPublicNoticesByStatus(PublicNoticeStatus.WAITING);
    var tomorrow = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    return publicNoticeDatesRepository.getAllByPublicNoticeInAndPublicationStartTimestampBeforeAndEndedByPersonIdIsNull(
        waitingPublicNotices, tomorrow)
        .stream().map(PublicNoticeDate::getPublicNotice)
        .collect(Collectors.toList());
  }

  public List<PublicNotice> getAllPublicNoticesDueToEnd() {

    var publishedPublicNotices = getPublicNoticesByStatus(PublicNoticeStatus.PUBLISHED);
    var tomorrow = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    return publicNoticeDatesRepository.getAllByPublicNoticeInAndPublicationEndTimestampBeforeAndEndedByPersonIdIsNull(
        publishedPublicNotices, tomorrow)
        .stream().map(PublicNoticeDate::getPublicNotice)
        .collect(Collectors.toList());
  }

  public void savePublicNoticeRequest(PublicNoticeRequest publicNoticeRequest) {
    publicNoticeRequestRepository.save(publicNoticeRequest);
  }

  public PublicNotice savePublicNotice(PublicNotice publicNotice) {
    return publicNoticeRepository.save(publicNotice);
  }

  public PublicNoticeDocument savePublicNoticeDocument(PublicNoticeDocument publicNoticeDocument) {
    return publicNoticeDocumentRepository.save(publicNoticeDocument);
  }

  public PublicNoticeDocumentLink savePublicNoticeDocumentLink(PublicNoticeDocumentLink publicNoticeDocumentLink) {
    return publicNoticeDocumentLinkRepository.save(publicNoticeDocumentLink);
  }

  public void endPublicNotices(List<PublicNotice> publicNotices) {
    publicNotices.forEach(publicNotice -> publicNotice.setStatus(PublicNoticeStatus.ENDED));
    publicNoticeRepository.saveAll(publicNotices);
  }

  void archivePublicNoticeDocument(PublicNoticeDocument publicNoticeDocument) {
    publicNoticeDocument.setDocumentType(PublicNoticeDocumentType.ARCHIVED);
    publicNoticeDocumentRepository.save(publicNoticeDocument);
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

      try {
        var appFileView = getPublicNoticeDocumentFileViewForPublicNotice(draftPublicNoticeOpt.get(), pwaApplication);
        appFileService.mapFileToForm(form, appFileView);
      } catch (EntityLatestVersionNotFoundException e) {
        // do nothing here if there is no doc associated with the PN
      }
    }

    form.setCoverLetterText(coverLetterText);
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
    return latestPublicNoticeOptional.map(publicNotice ->
        isPublicNoticeStatusEnded(publicNotice.getStatus()) || PublicNoticeStatus.DRAFT.equals(publicNotice.getStatus()))
        .orElse(true);
  }

  public boolean isPublicNoticeStatusEnded(PublicNoticeStatus publicNoticeStatus) {
    return ENDED_STATUSES.contains(publicNoticeStatus);
  }

  public boolean canApplicantViewLatestPublicNotice(PwaApplication pwaApplication) {
    return getLatestPublicNoticeOpt(pwaApplication)
       .map(pn -> APPLICANT_VIEW_STATUSES.contains(pn.getStatus()))
       .orElse(false);
  }

  private PublicNoticeView createViewFromPublicNotice(PublicNotice publicNotice) {

    var publicNoticeRequest = getLatestPublicNoticeRequest(publicNotice);
    var latestDocumentComments = publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(
        publicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT);
    String withdrawingPersonName = null;
    String withdrawnTimestamp = null;
    String withdrawalReason = null;
    String publicationStartTimestamp = null;
    String publicationEndTimestamp = null;
    String downloadUrl = null;


    if (publicNotice.getStatus().equals(PublicNoticeStatus.WITHDRAWN)) {
      withdrawingPersonName = personService.getPersonById(publicNotice.getWithdrawingPersonId()).getFullName();
      withdrawnTimestamp = DateUtils.formatDate(publicNotice.getWithdrawalTimestamp());
      withdrawalReason = publicNotice.getWithdrawalReason();
      downloadUrl = getArchivedPublicNoticeDocumentDownloadUrl(publicNotice);
    }

    var publicNoticeDateOpt = publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publicNotice);
    if (publicNoticeDateOpt.isPresent()) {
      var publicNoticeDate = publicNoticeDateOpt.get();
      publicationStartTimestamp = DateUtils.formatDate(publicNoticeDate.getPublicationStartTimestamp());
      publicationEndTimestamp = DateUtils.formatDate(publicNoticeDate.getPublicationEndTimestamp());
    }

    return new PublicNoticeView(
        publicNotice.getStatus(),
        DateUtils.formatDateTime(publicNoticeRequest.getCreatedTimestamp()),
        latestDocumentComments.map(PublicNoticeDocument::getComments).orElse(null),
        withdrawingPersonName,
        withdrawnTimestamp,
        withdrawalReason,
        publicationStartTimestamp,
        publicationEndTimestamp,
        publicNoticeRequest.getStatus(),
        publicNoticeRequest.getRejectionReason(),
        downloadUrl
    );
  }

  @VisibleForTesting
  Set<PublicNoticeAction> getAvailablePublicNoticeActions(PublicNoticeStatus publicNoticeStatus,
                                                          PwaAppProcessingContext pwaAppProcessingContext) {

    if (!getTaskState(pwaAppProcessingContext).equals(TaskState.EDIT)) {
      return Set.of();
    }

    var processingPermissions = pwaAppProcessingContext.getAppProcessingPermissions();
    Set<PublicNoticeAction> publicNoticeActions = new HashSet<>();

    if (processingPermissions.contains(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)
        && (publicNoticeStatus == null || isPublicNoticeStatusEnded(publicNoticeStatus))) {
      return Set.of(PublicNoticeAction.NEW_DRAFT);
    }

    var pwaApplication = pwaAppProcessingContext.getPwaApplication();
    var fileView = getLatestPublicNoticeDocumentFileViewIfExists(pwaApplication);

    if (processingPermissions.contains(PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES) && fileView.isPresent()) {
      publicNoticeActions.add(PublicNoticeAction.DOWNLOAD);
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

  private UploadedFileView getPublicNoticeDocumentFileViewForPublicNotice(PublicNotice publicNotice, PwaApplication pwaApplication) {
    var latestPublicNoticeDocument = getLatestPublicNoticeDocument(publicNotice);
    var documentLink = publicNoticeDocumentLinkRepository.findByPublicNoticeDocument(latestPublicNoticeDocument)
        .orElseThrow(() -> new EntityLatestVersionNotFoundException(String.format(
            "Couldn't find public notice document link with public notice document ID: %s", latestPublicNoticeDocument.getId())));

    return appFileService.getUploadedFileView(
        pwaApplication, documentLink.getAppFile().getFileId(), FILE_PURPOSE, ApplicationFileLinkStatus.FULL);
  }

  public UploadedFileView getLatestPublicNoticeDocumentFileView(PwaApplication pwaApplication) {

    var publicNotice = getLatestPublicNotice(pwaApplication);
    return getPublicNoticeDocumentFileViewForPublicNotice(publicNotice, pwaApplication);
  }

  public Optional<PublicNoticeDocument> getLatestPublicNoticeDocumentIfExists(PublicNotice publicNotice) {
    return publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(publicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT);
  }

  private Optional<UploadedFileView> getPublicNoticeDocumentFileViewForPublicNoticeIfExists(PublicNotice publicNotice,
                                                                                            PwaApplication pwaApplication) {
    var latestPublicNoticeDocumentOptional = getLatestPublicNoticeDocumentIfExists(publicNotice);

    if (latestPublicNoticeDocumentOptional.isPresent()) {
      var latestPublicNoticeDocument = latestPublicNoticeDocumentOptional.get();
      var documentLink = publicNoticeDocumentLinkRepository.findByPublicNoticeDocument(latestPublicNoticeDocument)
          .orElseThrow(() -> new EntityLatestVersionNotFoundException(String.format(
              "Couldn't find public notice document link with public notice document ID: %s", latestPublicNoticeDocument.getId())));

      return Optional.of(appFileService
          .getUploadedFileView(pwaApplication, documentLink.getAppFile().getFileId(), FILE_PURPOSE, ApplicationFileLinkStatus.FULL));
    }
    return Optional.empty();
  }

  public Optional<UploadedFileView> getLatestPublicNoticeDocumentFileViewIfExists(PwaApplication pwaApplication) {

    var publicNotice = getLatestPublicNotice(pwaApplication);
    return getPublicNoticeDocumentFileViewForPublicNoticeIfExists(publicNotice, pwaApplication);
  }

  private PublicNoticeDocument getArchivedPublicNoticeDocument(PublicNotice publicNotice) {
    return publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(publicNotice, PublicNoticeDocumentType.ARCHIVED)
        .orElseThrow(() -> new EntityLatestVersionNotFoundException(String.format(
            "Couldn't find public notice document with public notice ID: %s", publicNotice.getId())));
  }

  private UploadedFileView getPublicNoticeDocumentFileViewForArchivedPublicNotice(PublicNotice publicNotice) {
    var publicNoticeDocument = getArchivedPublicNoticeDocument(publicNotice);
    var documentLink = publicNoticeDocumentLinkRepository.findByPublicNoticeDocument(publicNoticeDocument)
        .orElseThrow(() -> new EntityLatestVersionNotFoundException(String.format(
            "Couldn't find public notice document link with public notice document ID: %s", publicNoticeDocument.getId())));

    return appFileService.getUploadedFileView(
        publicNotice.getPwaApplication(), documentLink.getAppFile().getFileId(), FILE_PURPOSE, ApplicationFileLinkStatus.FULL);
  }

  private String getArchivedPublicNoticeDocumentDownloadUrl(PublicNotice publicNotice) {
    var pwaApplication = publicNotice.getPwaApplication();
    var fileId = getPublicNoticeDocumentFileViewForArchivedPublicNotice(publicNotice).getFileId();

    return ReverseRouter.route(on(PublicNoticeDraftController.class)
        .handleDownload(pwaApplication.getApplicationType(), pwaApplication.getId(), fileId, null));
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
