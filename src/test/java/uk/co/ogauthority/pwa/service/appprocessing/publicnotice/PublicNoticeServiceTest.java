package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;
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
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileViewTestUtil;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticeApprovalRequestEmailProps;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDatesRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextTestUtil;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.template.TemplateTextService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeDraftValidator;

@RunWith(MockitoJUnitRunner.class)
public class PublicNoticeServiceTest {

  private PublicNoticeService publicNoticeService;

  @Mock
  private TemplateTextService templateTextService;

  @Mock
  private PublicNoticeDraftValidator validator;

  @Mock
  private AppFileService appFileService;

  @Mock
  private PublicNoticeRepository publicNoticeRepository;

  @Mock
  private PublicNoticeRequestRepository publicNoticeRequestRepository;

  @Mock
  private PublicNoticeDocumentRepository publicNoticeDocumentRepository;

  @Mock
  private PublicNoticeDocumentLinkRepository publicNoticeDocumentLinkRepository;

  @Mock
  private PublicNoticeDatesRepository publicNoticeDatesRepository;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private Clock clock;

  @Mock
  private NotifyService notifyService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Mock
  private PwaTeamService pwaTeamService;

  @Mock
  private PersonService personService;

  @Captor
  private ArgumentCaptor<PublicNoticeApprovalRequestEmailProps> approvalRequestEmailPropsCaptor;

  @Captor
  private ArgumentCaptor<String> emailAddressCaptor;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private static AppFilePurpose FILE_PURPOSE = AppFilePurpose.PUBLIC_NOTICE;

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocument> publicNoticeDocumentArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocumentLink> publicNoticeDocumentLinkArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeRequest> publicNoticeRequestArgumentCaptor;

  private static Set<PublicNoticeStatus> ENDED_STATUSES;


  @Before
  public void setUp() {
    clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    publicNoticeService = new PublicNoticeService(
        templateTextService,
        validator,
        appFileService,
        publicNoticeRepository,
        publicNoticeRequestRepository,
        publicNoticeDocumentRepository,
        publicNoticeDocumentLinkRepository,
        publicNoticeDatesRepository, camundaWorkflowService,
        clock, notifyService, emailCaseLinkService, pwaTeamService, personService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
    ENDED_STATUSES = Set.of(PublicNoticeStatus.ENDED, PublicNoticeStatus.WITHDRAWN);
  }

  @Test
  public void canShowInTaskList_draftPublicNoticePermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE), null, null);

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_caseManagementIndustryPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null);

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_approvePublicNoticePermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE), null, null);

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermissions_false() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null);

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry_notSatisfactory() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication()));

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PUBLIC_NOTICE.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.PUBLIC_NOTICE.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.CANNOT_START_YET));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_publicNoticeNotStarted() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()));

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PUBLIC_NOTICE.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.PUBLIC_NOTICE.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }


  @Test
  public void getPublicNoticesByStatus() {
    publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.DRAFT);
    verify(publicNoticeRepository, times(1)).findAllByStatus(PublicNoticeStatus.DRAFT);
  }

  @Test
  public void getOpenPublicNotices() {
    publicNoticeService.getOpenPublicNotices();
    verify(publicNoticeRepository, times(1)).findAllByStatusNotIn(ENDED_STATUSES);
  }

  @Test(expected = EntityLatestVersionNotFoundException.class)
  public void getLatestPublicNotice_notFound() {
    publicNoticeService.getLatestPublicNotice(pwaApplication);
    verify(publicNoticeRepository, times(1)).findFirstByPwaApplicationOrderByVersionDesc(pwaApplication);
  }

  @Test
  public void getLatestPublicNoticeDate_noExceptionThrown() {
    var publicNotice = PublicNoticeTestUtil.createWaitingPublicNotice(pwaApplication);
    when(publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publicNotice)).thenReturn(
        Optional.of(PublicNoticeTestUtil.createLatestPublicNoticeDate(publicNotice)));
    publicNoticeService.getLatestPublicNoticeDate(publicNotice);
    verify(publicNoticeDatesRepository, times(1)).getByPublicNoticeAndEndedByPersonIdIsNull(publicNotice);
  }

  @Test(expected = EntityLatestVersionNotFoundException.class)
  public void getLatestPublicNoticeDate_notFound() {
    publicNoticeService.getLatestPublicNoticeDate(new PublicNotice());
    verify(publicNoticeRepository, times(1)).findFirstByPwaApplicationOrderByVersionDesc(pwaApplication);
  }

  @Test
  public void getLatestPublicNotice_noExceptionThrown() {
    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(
        Optional.of(new PublicNotice()));
    publicNoticeService.getLatestPublicNotice(pwaApplication);
    verify(publicNoticeRepository, times(1)).findFirstByPwaApplicationOrderByVersionDesc(pwaApplication);
  }

  @Test
  public void archivePublicNoticeDocument() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    var publicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    publicNoticeService.archivePublicNoticeDocument(publicNoticeDocument);

    verify(publicNoticeDocumentRepository, times(1)).save(publicNoticeDocumentArgumentCaptor.capture());
    var actualPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getValue();
    assertThat(actualPublicNoticeDocument.getDocumentType()).isEqualTo(PublicNoticeDocumentType.ARCHIVED);
  }

  @Test
  public void mapPublicNoticeDraftToForm_publicNoticeRequestExists() {

    var coverLetterTemplateText = "cover letter text...";
    when(templateTextService.getLatestVersionTextByType(TemplateTextType.PUBLIC_NOTICE_COVER_LETTER)).thenReturn(coverLetterTemplateText);

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeRepository.findByStatusAndPwaApplication(PublicNoticeStatus.DRAFT, pwaApplication)).thenReturn(Optional.of(publicNotice));

    var expectedForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm();
    var publicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(publicNotice, expectedForm);
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(publicNotice)).thenReturn(Optional.of(publicNoticeRequest));


    var actualForm = new PublicNoticeDraftForm();
    publicNoticeService.mapPublicNoticeDraftToForm(pwaApplication, actualForm);

    assertThat(actualForm.getCoverLetterText()).isEqualTo(expectedForm.getCoverLetterText());
    assertThat(actualForm.getReason()).isEqualTo(expectedForm.getReason());
    assertThat(actualForm.getReasonDescription()).isEqualTo(expectedForm.getReasonDescription());
    verify(appFileService, times(1)).mapFilesToForm(expectedForm, pwaApplication, FILE_PURPOSE);
  }

  @Test
  public void mapPublicNoticeDraftToForm_publicNoticeRequestDoesNotExist() {

    var coverLetterTemplateText = "cover letter text...";
    when(templateTextService.getLatestVersionTextByType(TemplateTextType.PUBLIC_NOTICE_COVER_LETTER)).thenReturn(coverLetterTemplateText);

    when(publicNoticeRepository.findByStatusAndPwaApplication(PublicNoticeStatus.DRAFT, pwaApplication)).thenReturn(Optional.empty());

    var actualForm = new PublicNoticeDraftForm();
    publicNoticeService.mapPublicNoticeDraftToForm(pwaApplication, actualForm);

    assertThat(actualForm.getCoverLetterText()).isEqualTo(coverLetterTemplateText);
    assertThat(actualForm.getReason()).isNull();
    assertThat(actualForm.getReasonDescription()).isNull();
    verifyNoInteractions(appFileService);
    verifyNoInteractions(publicNoticeRequestRepository);
  }



  @Test
  public void createPublicNoticeAndStartWorkflow_newEntities_firstVersions() {

    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(Optional.empty());
    var expectedPublicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeRepository.save(expectedPublicNotice)).thenReturn(expectedPublicNotice);

    var expectedPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(expectedPublicNotice);
    when(publicNoticeDocumentRepository.save(expectedPublicNoticeDocument)).thenReturn(expectedPublicNoticeDocument);

    var publicNoticeAppFile = PublicNoticeTestUtil.createAppFileForPublicNotice(pwaApplication);
    var uploadFileWithDescriptionForm = new UploadFileWithDescriptionForm(
        publicNoticeAppFile.getFileId(), "desc", clock.instant());
    when(appFileService.getAppFileByPwaApplicationAndFileId(pwaApplication, publicNoticeAppFile.getFileId()))
        .thenReturn(publicNoticeAppFile);

    when(publicNoticeRepository.save(expectedPublicNotice)).thenReturn(expectedPublicNotice);

    String caseManagementLink = "case management link url";
    when(emailCaseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);

    var pwaManager1 = PersonTestUtil.createPersonFrom(new PersonId(1));
    var pwaManager2 = PersonTestUtil.createPersonFrom(new PersonId(2));
    var pwaManagers = Set.of(pwaManager1, pwaManager2);
    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER)).thenReturn(pwaManagers);

    var publicNoticeDraftForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm(List.of(uploadFileWithDescriptionForm));
    publicNoticeService.createPublicNoticeAndStartWorkflow(publicNoticeDraftForm, pwaApplication, user);


    verify(publicNoticeRepository, times(1)).save(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice).isEqualTo(expectedPublicNotice);

    verify(publicNoticeDocumentRepository, times(1)).save(publicNoticeDocumentArgumentCaptor.capture());
    var actualPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getValue();
    assertThat(actualPublicNoticeDocument).isEqualTo(expectedPublicNoticeDocument);

    verify(publicNoticeDocumentLinkRepository, times(1)).save(publicNoticeDocumentLinkArgumentCaptor.capture());
    var actualPublicNoticeDocumentLink = publicNoticeDocumentLinkArgumentCaptor.getValue();
    assertThat(actualPublicNoticeDocumentLink.getPublicNoticeDocument()).isEqualTo(actualPublicNoticeDocument);
    assertThat(actualPublicNoticeDocumentLink.getAppFile()).isEqualTo(publicNoticeAppFile);

    verify(publicNoticeRequestRepository, times(1)).save(publicNoticeRequestArgumentCaptor.capture());
    var publicNoticeRequest = publicNoticeRequestArgumentCaptor.getValue();
    assertThat(publicNoticeRequest.getPublicNotice()).isEqualTo(actualPublicNotice);
    assertThat(publicNoticeRequest.getCoverLetterText()).isEqualTo(publicNoticeDraftForm.getCoverLetterText());
    assertThat(publicNoticeRequest.getStatus()).isEqualTo(PublicNoticeRequestStatus.WAITING_MANAGER_APPROVAL);
    assertThat(publicNoticeRequest.getReason()).isEqualTo(publicNoticeDraftForm.getReason());
    assertThat(publicNoticeRequest.getReasonDescription()).isEqualTo(publicNoticeDraftForm.getReasonDescription());
    assertThat(publicNoticeRequest.getVersion()).isEqualTo(1);
    assertThat(publicNoticeRequest.getSubmittedTimestamp()).isEqualTo(clock.instant());
    assertThat(publicNoticeRequest.getCreatedByPersonId()).isEqualTo(user.getLinkedPerson().getId().asInt());

    verify(camundaWorkflowService, times(1)).startWorkflow(expectedPublicNotice);

    verify(notifyService, times(pwaManagers.size())).sendEmail(approvalRequestEmailPropsCaptor.capture(), emailAddressCaptor.capture());

    assertThat(approvalRequestEmailPropsCaptor.getAllValues()).allSatisfy(emailProps -> {

      assertThat(emailProps.getEmailPersonalisation()).contains(
          entry("APPLICATION_REFERENCE", pwaApplication.getAppReference()),
          entry("PUBLIC_NOTICE_REASON", publicNoticeDraftForm.getReason().getReasonText()),
          entry("CASE_MANAGEMENT_LINK", caseManagementLink)
      );

      assertThat(emailProps.getTemplate()).isEqualTo(NotifyTemplate.PUBLIC_NOTICE_APPROVAL_REQUEST);

    });

    assertThat(approvalRequestEmailPropsCaptor.getAllValues().get(0).getRecipientFullName()).isEqualTo(pwaManager1.getFullName());
    assertThat(emailAddressCaptor.getAllValues().get(0)).isEqualTo(pwaManager1.getEmailAddress());

    assertThat(approvalRequestEmailPropsCaptor.getAllValues().get(1).getRecipientFullName()).isEqualTo(pwaManager2.getFullName());
    assertThat(emailAddressCaptor.getAllValues().get(1)).isEqualTo(pwaManager2.getEmailAddress());

  }

  @Test
  public void createPublicNoticeDocumentLinkFromForm() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    var document = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);

    var publicNoticeAppFile = PublicNoticeTestUtil.createAppFileForPublicNotice(pwaApplication);
    var uploadFileWithDescriptionForm = new UploadFileWithDescriptionForm(
        publicNoticeAppFile.getFileId(), "desc", clock.instant());
    when(appFileService.getAppFileByPwaApplicationAndFileId(pwaApplication, publicNoticeAppFile.getFileId()))
        .thenReturn(publicNoticeAppFile);

    var documentLink = publicNoticeService.createPublicNoticeDocumentLinkFromForm(pwaApplication, uploadFileWithDescriptionForm, document);
    assertThat(documentLink.getPublicNoticeDocument()).isEqualTo(document);
    assertThat(documentLink.getAppFile()).isEqualTo(publicNoticeAppFile);
  }

  @Test
  public void canCreatePublicNoticeDraft_noPublicNoticeExists_canCreateDraftIsTrue() {

    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(Optional.empty());
    var canCreateDraft = publicNoticeService.canCreatePublicNoticeDraft(pwaApplication);
    assertThat(canCreateDraft).isTrue();
  }

  @Test
  public void canCreatePublicNoticeDraft_latestPublicNoticeEnded_canCreateDraftIsTrue() {

    var publicNotice = PublicNoticeTestUtil.createWithdrawnPublicNotice(pwaApplication);
    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(Optional.of(publicNotice));
    var canCreateDraft = publicNoticeService.canCreatePublicNoticeDraft(pwaApplication);
    assertThat(canCreateDraft).isTrue();
  }

  @Test
  public void canCreatePublicNoticeDraft_latestPublicNoticeNotEndedStatuses_canCreateDraftIsFalse() {

    EnumSet.complementOf(EnumSet.copyOf(ENDED_STATUSES)).forEach(activeStatus -> {
      var publicNotice = new PublicNotice();
      publicNotice.setStatus(activeStatus);
      when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(Optional.of(publicNotice));
      var canCreateDraft = publicNoticeService.canCreatePublicNoticeDraft(pwaApplication);
      assertThat(canCreateDraft).isFalse();
    });
  }


  @Test
  public void getAvailablePublicNoticeActions_draftPermissionAndNullStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(null, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.NEW_DRAFT);
  }

  @Test
  public void getAvailablePublicNoticeActions_draftPermissionAndEndedStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.ENDED, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.NEW_DRAFT);
  }

  @Test
  public void getAvailablePublicNoticeActions_draftPermissionAndActiveStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.APPLICANT_UPDATE, context);

    assertThat(publicNoticeActions).isEmpty();
  }

  @Test
  public void getAvailablePublicNoticeActions_draftPermissionAndDraftStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.DRAFT, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.UPDATE_DRAFT);
  }

  @Test
  public void getAvailablePublicNoticeActions_approvePermissionAndApprovalStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.MANAGER_APPROVAL, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.APPROVE);
  }

  @Test
  public void getAvailablePublicNoticeActions_additionalWithdrawPermissions_draftAndWithdrawActionsReturned() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE, PwaAppProcessingPermission.WITHDRAW_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.DRAFT, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.UPDATE_DRAFT, PublicNoticeAction.WITHDRAW);
  }

  @Test
  public void getAvailablePublicNoticeActions_requestUpdatePermissionAndCaseOfficerReviewStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.REQUEST_PUBLIC_NOTICE_UPDATE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.CASE_OFFICER_REVIEW, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.REQUEST_DOCUMENT_UPDATE);
  }

  @Test
  public void getAvailablePublicNoticeActions_finalisePermissionAndCaseOfficerReviewStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.CASE_OFFICER_REVIEW, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.FINALISE);
  }

  @Test
  public void getAvailablePublicNoticeActions_finalisePermissionAndWaitingStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.WAITING, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.UPDATE_DATES);
  }

  @Test
  public void getAvailablePublicNoticeActions_requestUpdateAndFinalisePermissions_updateAndFinaliseActionsReturned() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        Set.of(PwaAppProcessingPermission.REQUEST_PUBLIC_NOTICE_UPDATE, PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.CASE_OFFICER_REVIEW, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.REQUEST_DOCUMENT_UPDATE, PublicNoticeAction.FINALISE);
  }


  @Test
  public void getAllPublicNoticeViews_noPublicNotices() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE));

    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(context);
    assertThat(allPublicNoticesView.getCurrentPublicNotice()).isNull();
    assertThat(allPublicNoticesView.getHistoricalPublicNotices()).isEmpty();
    assertThat(allPublicNoticesView.getActions()).containsOnly(PublicNoticeAction.NEW_DRAFT);
  }

  @Test
  public void getAllPublicNoticeViews_currentPublicNoticeAndEndedPublicNotices_withdrawalPropertiesMatchPublicNotice() {

    var currentPublicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    var endedPublicNotice1 = PublicNoticeTestUtil.createWithdrawnPublicNotice(pwaApplication);
    var endedPublicNotice2 = PublicNoticeTestUtil.createWithdrawnPublicNotice(pwaApplication);
    when(publicNoticeRepository.findAllByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(
        List.of(currentPublicNotice, endedPublicNotice1 ,endedPublicNotice2));

    var currentPublicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(currentPublicNotice);
    var endedPublicNotice1Request = PublicNoticeTestUtil.createInitialPublicNoticeRequest(endedPublicNotice1);
    var endedPublicNotice2Request = PublicNoticeTestUtil.createInitialPublicNoticeRequest(endedPublicNotice2);
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(currentPublicNotice))
        .thenReturn(Optional.of(currentPublicNoticeRequest));
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(endedPublicNotice1))
        .thenReturn(Optional.of(endedPublicNotice1Request));
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(endedPublicNotice2))
        .thenReturn(Optional.of(endedPublicNotice2Request));

    var withdrawingPerson = PersonTestUtil.createDefaultPerson();
    when(personService.getPersonById(endedPublicNotice1.getWithdrawingPersonId())).thenReturn(withdrawingPerson);
    when(personService.getPersonById(endedPublicNotice2.getWithdrawingPersonId())).thenReturn(withdrawingPerson);


    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE));
    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(context);

    var expectedCurrentPublicNoticeView = PublicNoticeTestUtil.createCommentedPublicNoticeView(currentPublicNotice, currentPublicNoticeRequest);
    var expectedEndedPublicNotice1View = PublicNoticeTestUtil.createWithdrawnPublicNoticeView(
        endedPublicNotice1, withdrawingPerson.getFullName(), endedPublicNotice1Request);
    var expectedEndedPublicNotice2View = PublicNoticeTestUtil.createWithdrawnPublicNoticeView(
        endedPublicNotice2, withdrawingPerson.getFullName(), endedPublicNotice2Request);


    assertThat(allPublicNoticesView.getCurrentPublicNotice()).isEqualTo(expectedCurrentPublicNoticeView);
    assertThat(allPublicNoticesView.getHistoricalPublicNotices()).containsOnly(expectedEndedPublicNotice1View, expectedEndedPublicNotice2View);
    assertThat(allPublicNoticesView.getActions()).containsOnly(PublicNoticeAction.APPROVE);
  }

  @Test
  public void getAllPublicNoticeViews_currentPublicNoticeOnly() {

    var currentPublicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeRepository.findAllByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(List.of(currentPublicNotice));

    var currentPublicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(currentPublicNotice);
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(currentPublicNotice))
        .thenReturn(Optional.of(currentPublicNoticeRequest));


    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE));
    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(context);

    var expectedCurrentPublicNoticeView = PublicNoticeTestUtil.createCommentedPublicNoticeView(currentPublicNotice, currentPublicNoticeRequest);

    assertThat(allPublicNoticesView.getCurrentPublicNotice()).isEqualTo(expectedCurrentPublicNoticeView);
    assertThat(allPublicNoticesView.getHistoricalPublicNotices()).isEmpty();
    assertThat(allPublicNoticesView.getActions()).containsOnly(PublicNoticeAction.APPROVE);
  }

  @Test
  public void getAllPublicNoticeViews_publicNoticeDocumentHasComments_commentsStoredOnView() {

    var currentPublicNotice = PublicNoticeTestUtil.createApplicantUpdatePublicNotice(pwaApplication);
    when(publicNoticeRepository.findAllByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(List.of(currentPublicNotice));

    var currentPublicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(currentPublicNotice);
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(currentPublicNotice))
        .thenReturn(Optional.of(currentPublicNoticeRequest));

    var currentPublicNoticeDocument = PublicNoticeTestUtil.createCommentedPublicNoticeDocument(currentPublicNotice);
    when(publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(currentPublicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT))
        .thenReturn(Optional.of(currentPublicNoticeDocument));

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.UPDATE_PUBLIC_NOTICE_DOC));
    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(context);

    var expectedCurrentPublicNoticeView = PublicNoticeTestUtil.createCommentedPublicNoticeView(
        currentPublicNotice, currentPublicNoticeRequest, currentPublicNoticeDocument);
    assertThat(allPublicNoticesView.getCurrentPublicNotice()).isEqualTo(expectedCurrentPublicNoticeView);
  }

  @Test
  public void getAllPublicNoticeViews_onlyEndedPublicNotices_withdrawalPropertiesMatchPublicNotice() {

    var endedPublicNotice1 = PublicNoticeTestUtil.createWithdrawnPublicNotice(pwaApplication);
    var endedPublicNotice2 = PublicNoticeTestUtil.createWithdrawnPublicNotice(pwaApplication);
    when(publicNoticeRepository.findAllByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(
        List.of(endedPublicNotice1 ,endedPublicNotice2));

    var endedPublicNotice1Request = PublicNoticeTestUtil.createInitialPublicNoticeRequest(endedPublicNotice1);
    var endedPublicNotice2Request = PublicNoticeTestUtil.createInitialPublicNoticeRequest(endedPublicNotice2);
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(endedPublicNotice1))
        .thenReturn(Optional.of(endedPublicNotice1Request));
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(endedPublicNotice2))
        .thenReturn(Optional.of(endedPublicNotice2Request));

    var withdrawingPerson = PersonTestUtil.createDefaultPerson();
    when(personService.getPersonById(endedPublicNotice1.getWithdrawingPersonId())).thenReturn(withdrawingPerson);
    when(personService.getPersonById(endedPublicNotice2.getWithdrawingPersonId())).thenReturn(withdrawingPerson);

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE));
    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(context);

    var expectedEndedPublicNotice1View = PublicNoticeTestUtil.createWithdrawnPublicNoticeView(
        endedPublicNotice1, withdrawingPerson.getFullName(), endedPublicNotice1Request);
    var expectedEndedPublicNotice2View = PublicNoticeTestUtil.createWithdrawnPublicNoticeView(
        endedPublicNotice2, withdrawingPerson.getFullName(), endedPublicNotice2Request);

    assertThat(allPublicNoticesView.getCurrentPublicNotice()).isNull();
    assertThat(allPublicNoticesView.getHistoricalPublicNotices()).containsOnly(expectedEndedPublicNotice1View, expectedEndedPublicNotice2View);
    assertThat(allPublicNoticesView.getActions()).containsOnly(PublicNoticeAction.NEW_DRAFT);
  }

  @Test
  public void getAllPublicNoticeViews_publicNoticeHasPublicationDates_publicationDatesPropertiesMatchPublicNoticeDate() {

    var publishedPublicNotice = PublicNoticeTestUtil.createPublishedPublicNotice(pwaApplication);
    when(publicNoticeRepository.findAllByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(
        List.of(publishedPublicNotice));

    var publishedPublicNoticeRequest = PublicNoticeTestUtil.createApprovedPublicNoticeRequest(publishedPublicNotice);
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(publishedPublicNotice))
        .thenReturn(Optional.of(publishedPublicNoticeRequest));

    var publicNoticeDate = PublicNoticeTestUtil.createLatestPublicNoticeDate(publishedPublicNotice);
    when(publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publishedPublicNotice)).thenReturn(Optional.of(publicNoticeDate));

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.WITHDRAW_PUBLIC_NOTICE));
    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(context);

    var expectedPublicNoticeView = PublicNoticeTestUtil.createPublishedPublicNoticeView(
        publishedPublicNotice, publicNoticeDate, publishedPublicNoticeRequest);

    assertThat(allPublicNoticesView.getCurrentPublicNotice()).isEqualTo(expectedPublicNoticeView);
  }


  @Test
  public void validate_serviceInteractions() {
    var form = new PublicNoticeDraftForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    publicNoticeService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);
  }

  @Test
  public void getPublicNoticeDocumentLink_repositoryInteraction() {
    var appFile = new AppFile();
    publicNoticeService.getPublicNoticeDocumentLink(appFile);
    verify(publicNoticeDocumentLinkRepository, times(1)).findByAppFile(appFile);
  }

  @Test(expected = EntityLatestVersionNotFoundException.class)
  public void getLatestPublicNoticeDocument_notFound() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    publicNoticeService.getLatestPublicNoticeDocument(publicNotice);
    verify(publicNoticeDocumentRepository, times(1)).findByPublicNoticeAndDocumentType(
        publicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT);
  }

  @Test
  public void getPublicNoticeDocumentFileView_documentLinkExists() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(
        Optional.of(publicNotice));

    var document = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(publicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT))
        .thenReturn(Optional.of(document));

    var publicNoticeAppFile = PublicNoticeTestUtil.createAppFileForPublicNotice(pwaApplication);
    var documentLink = new PublicNoticeDocumentLink(document, publicNoticeAppFile);
    when(publicNoticeDocumentLinkRepository.findByPublicNoticeDocument(document)).thenReturn(Optional.of(documentLink));

    var documentFileView = UploadedFileViewTestUtil.createDefaultFileView();
    when(appFileService.getUploadedFileView(pwaApplication, documentLink.getAppFile().getFileId(), FILE_PURPOSE, ApplicationFileLinkStatus.FULL))
        .thenReturn(documentFileView);

    var actualFileView = publicNoticeService.getLatestPublicNoticeDocumentFileView(pwaApplication);
    assertThat(actualFileView).isEqualTo(documentFileView);
  }

  @Test(expected = EntityLatestVersionNotFoundException.class)
  public void getPublicNoticeDocumentFileView_documentLinkDoesNotExists() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(
        Optional.of(publicNotice));

    var document = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(publicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT))
        .thenReturn(Optional.of(document));

    publicNoticeService.getLatestPublicNoticeDocumentFileView(pwaApplication);
  }

  @Test
  public void deleteFileLinkAndPublicNoticeDocument_repositoryInteractions() {
    var publicNoticeDocumentLink = new PublicNoticeDocumentLink(new PublicNoticeDocument(), null);
    publicNoticeService.deleteFileLinkAndPublicNoticeDocument(publicNoticeDocumentLink);
    verify(publicNoticeDocumentRepository, times(1)).delete(publicNoticeDocumentLink.getPublicNoticeDocument());
    verify(publicNoticeDocumentLinkRepository, times(1)).delete(publicNoticeDocumentLink);
  }

  @Test
  public void publicNoticeInProgress_no() {

    var endedNotice = new PublicNotice();
    endedNotice.setStatus(PublicNoticeStatus.ENDED);

    var withdrawnNotice = new PublicNotice();
    withdrawnNotice.setStatus(PublicNoticeStatus.WITHDRAWN);

    when(publicNoticeRepository.findAllByPwaApplication(any())).thenReturn(List.of(endedNotice, withdrawnNotice));

    boolean inProgress = publicNoticeService.publicNoticeInProgress(new PwaApplication());

    assertThat(inProgress).isFalse();

  }

  @Test
  public void publicNoticeInProgress_yes() {

    var endedNotice = new PublicNotice();
    endedNotice.setStatus(PublicNoticeStatus.PUBLISHED);

    var inProgressNotice = new PublicNotice();
    inProgressNotice.setStatus(PublicNoticeStatus.APPLICANT_UPDATE);

    when(publicNoticeRepository.findAllByPwaApplication(any())).thenReturn(List.of(endedNotice, inProgressNotice));

    boolean inProgress = publicNoticeService.publicNoticeInProgress(new PwaApplication());

    assertThat(inProgress).isTrue();

  }

}
