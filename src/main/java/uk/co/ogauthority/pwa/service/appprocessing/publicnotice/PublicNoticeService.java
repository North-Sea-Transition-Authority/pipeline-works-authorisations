package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import com.google.common.annotations.VisibleForTesting;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.TemplateTextType;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocumentLink;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.PublicNoticeApprovalRequestEmailProps;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.model.view.publicnotice.AllPublicNoticesView;
import uk.co.ogauthority.pwa.model.view.publicnotice.PublicNoticeView;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
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
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;
  private final NotifyService notifyService;
  private final EmailCaseLinkService emailCaseLinkService;
  private final TeamService teamService;
  private final PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

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
      CamundaWorkflowService camundaWorkflowService,
      @Qualifier("utcClock") Clock clock, NotifyService notifyService,
      EmailCaseLinkService emailCaseLinkService,
      TeamService teamService,
      PwaAppProcessingPermissionService pwaAppProcessingPermissionService) {
    this.templateTextService = templateTextService;
    this.publicNoticeDraftValidator = publicNoticeDraftValidator;
    this.appFileService = appFileService;
    this.publicNoticeRepository = publicNoticeRepository;
    this.publicNoticeRequestRepository = publicNoticeRequestRepository;
    this.publicNoticeDocumentRepository = publicNoticeDocumentRepository;
    this.publicNoticeDocumentLinkRepository = publicNoticeDocumentLinkRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.clock = clock;
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.teamService = teamService;
    this.pwaAppProcessingPermissionService = pwaAppProcessingPermissionService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    boolean atLeastOneSatisfactoryVersion = processingContext.getApplicationInvolvement().hasAtLeastOneSatisfactoryVersion();

    return new TaskListEntry(
        task.getTaskName(),
        atLeastOneSatisfactoryVersion ? task.getRoute(processingContext) : null,
        atLeastOneSatisfactoryVersion ? TaskTag.from(TaskStatus.NOT_STARTED) : TaskTag.from(TaskStatus.CANNOT_START_YET),
        task.getDisplayOrder());

  }


  private PublicNoticeRequest getLatestPublicNoticeRequest(PublicNotice publicNotice) {
    return publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(publicNotice)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "Couldn't find public notice request with public notice ID: %s", publicNotice.getId())));
  }

  private Integer getPublicNoticeLatestVersionNumber(PwaApplication pwaApplication) {
    var latestPublicNoticeOpt = publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication);
    return latestPublicNoticeOpt.isPresent() ? latestPublicNoticeOpt.get().getVersion() : 0;
  }

  public void mapPublicNoticeDraftToForm(PwaApplication pwaApplication, PublicNoticeDraftForm form) {

    var coverLetterText = templateTextService.getLatestVersionTextByType(TemplateTextType.PUBLIC_NOTICE_COVER_LETTER);
    var latestPublicNoticeOpt = publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication);

    if (latestPublicNoticeOpt.isPresent()) {
      var publicNoticeRequest = getLatestPublicNoticeRequest(latestPublicNoticeOpt.get());
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

    var pwaManagers = teamService.getTeamMembers(teamService.getRegulatorTeam()).stream()
        .filter(member -> member.getRoleSet().stream()
            .map(PwaRole::getName)
            .anyMatch(roleName -> roleName.equals(PwaRegulatorRole.PWA_MANAGER.getPortalTeamRoleName())))
        .map(PwaTeamMember::getPerson)
        .collect(Collectors.toSet());

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
        pwaApplication, PublicNoticeStatus.DRAFT, getPublicNoticeLatestVersionNumber(pwaApplication) + 1);
    publicNotice = publicNoticeRepository.save(publicNotice);

    appFileService.updateFiles(form, pwaApplication, FILE_PURPOSE, FileUpdateMode.DELETE_UNLINKED_FILES, userAccount);

    var publicNoticeDocVersion = 1;
    var publicNoticeDocument = new PublicNoticeDocument(
        publicNotice, publicNoticeDocVersion, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT);
    publicNoticeDocument = publicNoticeDocumentRepository.save(publicNoticeDocument);

    //accessing list at index 0 as validator ensures there is always and only 1 file
    var documentUploadedFileId = form.getUploadedFileWithDescriptionForms().get(0).getUploadedFileId();
    var docAppFile = appFileService.getAppFileByPwaApplicationAndFileId(pwaApplication, documentUploadedFileId);
    var publicNoticeDocumentLink = new PublicNoticeDocumentLink(publicNoticeDocument, docAppFile);
    publicNoticeDocumentLinkRepository.save(publicNoticeDocumentLink);

    var publicNoticeRequest = createInitialPublicNoticeRequestFromForm(form, publicNotice, userAccount.getLinkedPerson());
    publicNoticeRequestRepository.save(publicNoticeRequest);

    camundaWorkflowService.startWorkflow(publicNotice);
    sendPublicNoticeApprovalEmails(pwaApplication, form.getReason().getReasonText());
  }


  private PublicNoticeView createViewFromPublicNotice(PublicNotice publicNotice) {

    var publicNoticeRequest = getLatestPublicNoticeRequest(publicNotice);

    return new PublicNoticeView(
        publicNotice.getStatus(), DateUtils.formatDate(publicNoticeRequest.getSubmittedTimestamp()));
  }

  @VisibleForTesting
  Set<PublicNoticeAction> getAvailablePublicNoticeActions(PublicNoticeStatus publicNoticeStatus,
                                                          AuthenticatedUserAccount user,
                                                          PwaApplicationDetail pwaApplicationDetail) {

    var processingPermissions = pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)
        .getProcessingPermissions();

    if (processingPermissions.contains(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE) && publicNoticeStatus == null) {
      return Set.of(PublicNoticeAction.NEW_DRAFT);

    } else if (processingPermissions.contains(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)
        && publicNoticeStatus.equals(PublicNoticeStatus.DRAFT)) {
      return Set.of(PublicNoticeAction.UPDATE_DRAFT);

    } else if (processingPermissions.contains(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE)
        && publicNoticeStatus.equals(PublicNoticeStatus.MANAGER_APPROVAL)) {
      return Set.of(PublicNoticeAction.APPROVE);
    }

    return Set.of();
  }


  public AllPublicNoticesView getAllPublicNoticeViews(PwaApplicationDetail pwaApplicationDetail, AuthenticatedUserAccount userAccount) {

    var publicNotices = publicNoticeRepository.findAllByPwaApplicationOrderByVersionDesc(pwaApplicationDetail.getPwaApplication());
    PublicNoticeView currentPublicNotice = null;
    List<PublicNoticeView> historicalPublicNotices = new ArrayList<>();
    Set<PublicNoticeAction> availableActions = getAvailablePublicNoticeActions(null, userAccount, pwaApplicationDetail);

    for (var x = 0; x < publicNotices.size(); x++) {
      //public notices are ordered with the newest at first index so just check if active
      if (x == 0 && !ENDED_STATUSES.contains(publicNotices.get(x).getStatus())) {
        currentPublicNotice = createViewFromPublicNotice(publicNotices.get(x));
        availableActions = getAvailablePublicNoticeActions(publicNotices.get(x).getStatus(), userAccount, pwaApplicationDetail);

      } else {
        historicalPublicNotices.add(createViewFromPublicNotice(publicNotices.get(x)));
      }
    }

    return new AllPublicNoticesView(currentPublicNotice, historicalPublicNotices, availableActions);
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
