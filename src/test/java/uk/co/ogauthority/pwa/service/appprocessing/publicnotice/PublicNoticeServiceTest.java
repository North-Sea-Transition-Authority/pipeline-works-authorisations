package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.TemplateTextType;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocumentLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileViewTestUtil;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDatesRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeRequestRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.template.TemplateTextService;
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
  private Clock clock;

  @Mock
  private PersonService personService;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private static AppFilePurpose FILE_PURPOSE = AppFilePurpose.PUBLIC_NOTICE;

  @Captor
  private ArgumentCaptor<List<PublicNotice>> publicNoticesArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocument> publicNoticeDocumentArgumentCaptor;

  private static Set<PublicNoticeStatus> ENDED_STATUSES;
  private static Set<PublicNoticeStatus> APPLICANT_VIEW_STATUSES;
  private ApplicationInvolvementDto appInvolvementDto;


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
        publicNoticeDatesRepository,
        personService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    ENDED_STATUSES = Set.of(PublicNoticeStatus.ENDED, PublicNoticeStatus.WITHDRAWN);
    APPLICANT_VIEW_STATUSES = Set.of(PublicNoticeStatus.WAITING, PublicNoticeStatus.PUBLISHED, PublicNoticeStatus.ENDED);

    appInvolvementDto = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        pwaApplication, Set.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION));


  }

  @Test
  public void canShowInTaskList_draftPublicNoticePermission_true() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE), null, null,
        Set.of());

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_caseManagementIndustryPermission_true() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null,
        Set.of());

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_approvePublicNoticePermission_true() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE), null, null,
        Set.of());

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_showAllTasksPublicNoticePermission_validAppType_true() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY), null, null,
        Set.of());

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_viewAllPublicNoticePermission_true() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES), null, null,
        Set.of());

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_showAllTasksPublicNoticePermission_invalidAppType_false() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY), null, null,
        Set.of());

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_caseManagementIndustryPermission_invalidAppType_false() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null,
        Set.of());

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_caseOfficerPermission_invalidAppType_false() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(PwaAppProcessingPermission.CASE_OFFICER_REVIEW), null, null,
        Set.of());

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_noPermissions_false() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(), null, null, Set.of());

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }


  @Test
  public void getTaskListEntry_publicNoticeAtManagerApproval_taskStatusIsActionRequired() {

    var appInvolvementDto = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        pwaApplication, Set.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION));
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE), null, appInvolvementDto,
        Set.of());

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(processingContext.getPwaApplication()))
        .thenReturn(Optional.of(publicNotice));

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.ACTION_REQUIRED));
  }

  @Test
  public void getTaskListEntry_publicNoticeHasEndedTypeStatus_taskStatusIsCompleted() {

    var appInvolvementDto = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        pwaApplication, Set.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION));
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(), null, appInvolvementDto,
        Set.of());

    ENDED_STATUSES.forEach(status -> {
      var publicNotice = PublicNoticeTestUtil.createPublicNoticeWithStatus(status);
      when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(processingContext.getPwaApplication()))
          .thenReturn(Optional.of(publicNotice));

      var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);

      assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.COMPLETED));
    });

  }

  @Test
  public void getTaskListEntry_publicNoticeNotEndedAndNotAtApprovalStage_taskStatusIsInProgress() {

    var appInvolvementDto = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        pwaApplication, Set.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION));
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(), null, appInvolvementDto,
        Set.of());

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(processingContext.getPwaApplication()))
        .thenReturn(Optional.of(publicNotice));

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.IN_PROGRESS));
  }

  @Test
  public void getTaskListEntry_noPublicNotice_appSatisfactory_taskStatusIsNotStarted() {

    var appInvolvementDto = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        pwaApplication, Set.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION));
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(), null, appInvolvementDto,
        Set.of());

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PUBLIC_NOTICE.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.PUBLIC_NOTICE.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
  }

  @Test
  public void getTaskListEntry_noPublicNotice_appNotSatisfactory_taskStatusIsCannotStart() {

    var appInvolvementDto = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplication);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null, Set.of(), null, appInvolvementDto,
        Set.of());

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PUBLIC_NOTICE.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.PUBLIC_NOTICE.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.CANNOT_START_YET));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();
  }

  @Test
  public void getTaskListEntry_hasViewPermissionAndCorrectAppStatus_noSatisfactoryVersion_taskStateLocked() {

    var appInvolvementDto = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplication);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES), null, appInvolvementDto, Set.of());

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
  }

  @Test
  public void getTaskListEntry_hasViewPermissionAndSatisfactoryVersion_incorrectAppStatus_taskStateLocked() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES), null, appInvolvementDto, Set.of());

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
  }

  @Test
  public void getTaskListEntry_hasCorrectAppStatusAndSatisfactoryVersion_noViewPermission_taskStateLocked() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(), null, appInvolvementDto, Set.of());

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
  }

  @Test
  public void getTaskListEntry_hasCorrectAppStatusAndSatisfactoryVersionAndViewPermission_taskStateView() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES), null, appInvolvementDto, Set.of());

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.VIEW);
  }

  @Test
  public void getTaskListEntry_hasCorrectAppStatusAndSatisfactoryVersionAndEditPermission_taskStateEdit() {

    var processingContext = new PwaAppProcessingContext(pwaApplicationDetail, null,
        Set.of(PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE), null, appInvolvementDto, Set.of());

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
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
  public void getAllPublicNoticesDueForPublishing_verifyRepoInteraction() {

    var publicNotices = List.of(PublicNoticeTestUtil.createWaitingPublicNotice(pwaApplication),
        PublicNoticeTestUtil.createWaitingPublicNotice(new PwaApplication()));
    when(publicNoticeRepository.findAllByStatus(PublicNoticeStatus.WAITING)).thenReturn(publicNotices);

    publicNoticeService.getAllPublicNoticesDueForPublishing();

    var tomorrow = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    verify(publicNoticeDatesRepository, times(1)).getAllByPublicNoticeInAndPublicationStartTimestampBeforeAndEndedByPersonIdIsNull(
        publicNotices, tomorrow);
  }

  @Test
  public void getAllPublicNoticesDueToEnd_verifyRepoInteraction() {

    var publicNotices = List.of(PublicNoticeTestUtil.createPublishedPublicNotice(pwaApplication),
        PublicNoticeTestUtil.createPublishedPublicNotice(new PwaApplication()));
    when(publicNoticeRepository.findAllByStatus(PublicNoticeStatus.PUBLISHED)).thenReturn(publicNotices);

    publicNoticeService.getAllPublicNoticesDueToEnd();

    var tomorrow = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    verify(publicNoticeDatesRepository, times(1)).getAllByPublicNoticeInAndPublicationEndTimestampBeforeAndEndedByPersonIdIsNull(
        publicNotices, tomorrow);
  }

  @Test
  public void endPublicNotices_statusUpdatedAndSaved() {

    var publicNotices = List.of(PublicNoticeTestUtil.createPublishedPublicNotice(pwaApplication),
        PublicNoticeTestUtil.createPublishedPublicNotice(new PwaApplication()));
    publicNoticeService.endPublicNotices(publicNotices);

    verify(publicNoticeRepository, times(1)).saveAll(publicNoticesArgumentCaptor.capture());
    publicNoticesArgumentCaptor.getValue().forEach(
        actualPublicNotice -> assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.ENDED));
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

    var latestPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(publicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT))
        .thenReturn(Optional.of(latestPublicNoticeDocument));

    var publicNoticeAppFile = new AppFile();
    var publicNoticeDocumentLink = new PublicNoticeDocumentLink(latestPublicNoticeDocument, publicNoticeAppFile);
    when(publicNoticeDocumentLinkRepository.findByPublicNoticeDocument(latestPublicNoticeDocument)).thenReturn(Optional.of(publicNoticeDocumentLink));

    var appFileView = UploadedFileViewTestUtil.createDefaultFileView();
    when(appFileService.getUploadedFileView(
        pwaApplication, publicNoticeDocumentLink.getAppFile().getFileId(), FILE_PURPOSE, ApplicationFileLinkStatus.FULL))
        .thenReturn(appFileView);

    var actualForm = new PublicNoticeDraftForm();
    publicNoticeService.mapPublicNoticeDraftToForm(pwaApplication, actualForm);

    assertThat(actualForm.getCoverLetterText()).isEqualTo(expectedForm.getCoverLetterText());
    assertThat(actualForm.getReason()).isEqualTo(expectedForm.getReason());
    assertThat(actualForm.getReasonDescription()).isEqualTo(expectedForm.getReasonDescription());
    verify(appFileService).mapFileToForm(expectedForm, appFileView);
  }

  @Test
  public void mapPublicNoticeDraftToForm_publicNoticeRequestExists_noDocAssociated_noError() {

    var coverLetterTemplateText = "cover letter text...";
    when(templateTextService.getLatestVersionTextByType(TemplateTextType.PUBLIC_NOTICE_COVER_LETTER)).thenReturn(coverLetterTemplateText);

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeRepository.findByStatusAndPwaApplication(PublicNoticeStatus.DRAFT, pwaApplication)).thenReturn(Optional.of(publicNotice));

    var expectedForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm();
    var publicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(publicNotice, expectedForm);
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(publicNotice)).thenReturn(Optional.of(publicNoticeRequest));

    when(publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(publicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT))
        .thenThrow(EntityLatestVersionNotFoundException.class);

    var actualForm = new PublicNoticeDraftForm();
    publicNoticeService.mapPublicNoticeDraftToForm(pwaApplication, actualForm);

    assertThat(actualForm.getCoverLetterText()).isEqualTo(expectedForm.getCoverLetterText());
    assertThat(actualForm.getReason()).isEqualTo(expectedForm.getReason());
    assertThat(actualForm.getReasonDescription()).isEqualTo(expectedForm.getReasonDescription());
    verifyNoInteractions(appFileService);

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

    var publicNotice = PublicNoticeTestUtil.createDraftPublicNotice(pwaApplication);
    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(Optional.of(publicNotice));
    var canCreateDraft = publicNoticeService.canCreatePublicNoticeDraft(pwaApplication);
    assertThat(canCreateDraft).isTrue();
  }

  @Test
  public void canCreatePublicNoticeDraft_latestPublicNoticeIsDraft_canCreateDraftIsTrue() {

    var publicNotice = PublicNoticeTestUtil.createWithdrawnPublicNotice(pwaApplication);
    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(Optional.of(publicNotice));
    var canCreateDraft = publicNoticeService.canCreatePublicNoticeDraft(pwaApplication);
    assertThat(canCreateDraft).isTrue();
  }

  @Test
  public void canCreatePublicNoticeDraft_latestPublicNoticeNotEndedStatuses_canCreateDraftIsFalse() {

    var allowedStatuses = EnumSet.copyOf(ENDED_STATUSES);
    allowedStatuses.add(PublicNoticeStatus.DRAFT);
    EnumSet.complementOf(allowedStatuses).forEach(activeStatus -> {
      var publicNotice = new PublicNotice();
      publicNotice.setStatus(activeStatus);
      when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(Optional.of(publicNotice));
      var canCreateDraft = publicNoticeService.canCreatePublicNoticeDraft(pwaApplication);
      assertThat(canCreateDraft).isFalse();
    });
  }

  @Test
  public void canApplicantViewLatestPublicNotice_noPublicNoticeExists() {
    assertThat(publicNoticeService.canApplicantViewLatestPublicNotice(pwaApplication)).isFalse();
  }

  @Test
  public void canApplicantViewLatestPublicNotice_statusInvalid_cannotView() {

    EnumSet.complementOf(EnumSet.copyOf(APPLICANT_VIEW_STATUSES)).forEach(status -> {
      var publicNotice = new PublicNotice();
      publicNotice.setStatus(status);
      when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(Optional.of(publicNotice));
      var canViewPublicNotice = publicNoticeService.canApplicantViewLatestPublicNotice(pwaApplication);
      assertThat(canViewPublicNotice).isFalse();
    });
  }

  @Test
  public void canApplicantViewLatestPublicNotice_statusValid_canView() {

    APPLICANT_VIEW_STATUSES.forEach(status -> {
      var publicNotice = new PublicNotice();
      publicNotice.setStatus(status);
      when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(Optional.of(publicNotice));
      var canViewPublicNotice = publicNoticeService.canApplicantViewLatestPublicNotice(pwaApplication);
      assertThat(canViewPublicNotice).isTrue();
    });
  }

  @Test
  public void getAvailablePublicNoticeActions_taskStateNotEditable_noActions() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(null, context);

    assertThat(publicNoticeActions).isEmpty();
  }

  @Test
  public void getAvailablePublicNoticeActions_draftPermissionAndNullStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(null, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.NEW_DRAFT);
  }

  @Test
  public void getAvailablePublicNoticeActions_draftPermissionAndEndedStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.ENDED, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.NEW_DRAFT);
  }

  @Test
  public void getAvailablePublicNoticeActions_draftPermissionAndActiveStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.APPLICANT_UPDATE, context);

    assertThat(publicNoticeActions).isEmpty();
  }

  @Test
  public void getAvailablePublicNoticeActions_draftPermissionAndDraftStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.DRAFT, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.UPDATE_DRAFT);
  }

  @Test
  public void getAvailablePublicNoticeActions_approvePermissionAndApprovalStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.MANAGER_APPROVAL, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.APPROVE);
  }

  @Test
  public void getAvailablePublicNoticeActions_additionalWithdrawPermissions_draftAndWithdrawActionsReturned() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE,
            PwaAppProcessingPermission.WITHDRAW_PUBLIC_NOTICE,
            PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.DRAFT, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.UPDATE_DRAFT, PublicNoticeAction.WITHDRAW);
  }

  @Test
  public void getAvailablePublicNoticeActions_requestUpdatePermissionAndCaseOfficerReviewStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.REQUEST_PUBLIC_NOTICE_UPDATE, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.CASE_OFFICER_REVIEW, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.REQUEST_DOCUMENT_UPDATE);
  }

  @Test
  public void getAvailablePublicNoticeActions_finalisePermissionAndCaseOfficerReviewStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.CASE_OFFICER_REVIEW, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.FINALISE);
  }

  @Test
  public void getAvailablePublicNoticeActions_finalisePermissionAndWaitingStatus() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.WAITING, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.UPDATE_DATES);
  }

  @Test
  public void getAvailablePublicNoticeActions_requestUpdateAndFinalisePermissions_updateAndFinaliseActionsReturned() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        Set.of(PwaAppProcessingPermission.REQUEST_PUBLIC_NOTICE_UPDATE,
            PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE,
            PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.CASE_OFFICER_REVIEW, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.REQUEST_DOCUMENT_UPDATE, PublicNoticeAction.FINALISE);
  }

  @Test
  public void getAvailablePublicNoticeActions_viewPublicNoticePermissionsAndDocumentExists_downloadActionReturned() {

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

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        Set.of(PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.WAITING, context);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.DOWNLOAD);
  }

  @Test
  public void getAvailablePublicNoticeActions_documentDoesNotExist_downloadActionNotReturned() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(
        Optional.of(publicNotice));

    when(publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(publicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT))
        .thenReturn(Optional.empty());

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        Set.of(PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.WAITING, context);

    assertThat(publicNoticeActions).isEmpty();
  }

  @Test
  public void getAllPublicNoticeViews_noPublicNotices() {

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));

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

    var currentDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(currentPublicNotice);
    when(publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(currentPublicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT))
        .thenReturn(Optional.of(currentDocument));

    var document = PublicNoticeTestUtil.createInitialPublicNoticeDocument(endedPublicNotice1);
    when(publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(endedPublicNotice1, PublicNoticeDocumentType.ARCHIVED))
        .thenReturn(Optional.of(document));

    var document2 = PublicNoticeTestUtil.createInitialPublicNoticeDocument(endedPublicNotice2);
    when(publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(endedPublicNotice2, PublicNoticeDocumentType.ARCHIVED))
        .thenReturn(Optional.of(document2));

    var publicNoticeAppFile = PublicNoticeTestUtil.createAppFileForPublicNotice(pwaApplication);
    var documentLink = new PublicNoticeDocumentLink(document, publicNoticeAppFile);
    when(publicNoticeDocumentLinkRepository.findByPublicNoticeDocument(document)).thenReturn(Optional.of(documentLink));
    when(publicNoticeDocumentLinkRepository.findByPublicNoticeDocument(document2)).thenReturn(Optional.of(documentLink));

    var documentFileView = UploadedFileViewTestUtil.createDefaultFileView();
    when(appFileService.getUploadedFileView(pwaApplication, documentLink.getAppFile().getFileId(), FILE_PURPOSE, ApplicationFileLinkStatus.FULL))
        .thenReturn(documentFileView);

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(context);

    var expectedCurrentPublicNoticeView = PublicNoticeTestUtil.createCommentedPublicNoticeView(currentPublicNotice, currentPublicNoticeRequest);
    var expectedEndedPublicNotice1View = PublicNoticeTestUtil.createWithdrawnPublicNoticeView(
        endedPublicNotice1, withdrawingPerson.getFullName(), endedPublicNotice1.getWithdrawalReason(), endedPublicNotice1Request);
    var expectedEndedPublicNotice2View = PublicNoticeTestUtil.createWithdrawnPublicNoticeView(
        endedPublicNotice2, withdrawingPerson.getFullName(), endedPublicNotice2.getWithdrawalReason(), endedPublicNotice2Request);


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
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
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

    var document = PublicNoticeTestUtil.createInitialPublicNoticeDocument(endedPublicNotice1);
    when(publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(endedPublicNotice1, PublicNoticeDocumentType.ARCHIVED))
        .thenReturn(Optional.of(document));

    var document2 = PublicNoticeTestUtil.createInitialPublicNoticeDocument(endedPublicNotice2);
    when(publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(endedPublicNotice2, PublicNoticeDocumentType.ARCHIVED))
        .thenReturn(Optional.of(document2));

    var publicNoticeAppFile = PublicNoticeTestUtil.createAppFileForPublicNotice(pwaApplication);
    var documentLink = new PublicNoticeDocumentLink(document, publicNoticeAppFile);
    when(publicNoticeDocumentLinkRepository.findByPublicNoticeDocument(document)).thenReturn(Optional.of(documentLink));
    when(publicNoticeDocumentLinkRepository.findByPublicNoticeDocument(document2)).thenReturn(Optional.of(documentLink));

    var documentFileView = UploadedFileViewTestUtil.createDefaultFileView();
    when(appFileService.getUploadedFileView(pwaApplication, documentLink.getAppFile().getFileId(), FILE_PURPOSE, ApplicationFileLinkStatus.FULL))
        .thenReturn(documentFileView);

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE));
    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(context);

    var expectedEndedPublicNotice1View = PublicNoticeTestUtil.createWithdrawnPublicNoticeView(
        endedPublicNotice1, withdrawingPerson.getFullName(), endedPublicNotice1.getWithdrawalReason(), endedPublicNotice1Request);
    var expectedEndedPublicNotice2View = PublicNoticeTestUtil.createWithdrawnPublicNoticeView(
        endedPublicNotice2, withdrawingPerson.getFullName(), endedPublicNotice2.getWithdrawalReason(), endedPublicNotice2Request);

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
  public void getAllPublicNoticeViews_testPublicNoticeEvents() {

    var publishedPublicNotice = PublicNoticeTestUtil.createPublishedPublicNotice(pwaApplication);
    when(publicNoticeRepository.findAllByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(
        List.of(publishedPublicNotice));
    var person1 = new Person(1, "Person", "1", null, null);
    var person2 = new Person(2, "Person", "2", null, null);
    when(personService.findAllByIdIn(any())).thenReturn(List.of(person1, person2));

    var publishedPublicNoticeRequest = PublicNoticeTestUtil.createApprovedPublicNoticeRequest(publishedPublicNotice);
    publishedPublicNoticeRequest.setResponseTimestamp(publishedPublicNoticeRequest.getCreatedTimestamp().plus(1, ChronoUnit.HOURS));
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(publishedPublicNotice))
        .thenReturn(Optional.of(publishedPublicNoticeRequest));

    var publicNoticeDate = PublicNoticeTestUtil.createLatestPublicNoticeDate(publishedPublicNotice);
    when(publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publishedPublicNotice)).thenReturn(Optional.of(publicNoticeDate));

    var context = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail, Set.of(PwaAppProcessingPermission.WITHDRAW_PUBLIC_NOTICE));

    when(publicNoticeRequestRepository.findAllByPublicNotice(publishedPublicNotice)).thenReturn(List.of(publishedPublicNoticeRequest));
    when(publicNoticeDatesRepository.getAllByPublicNotice(publishedPublicNotice)).thenReturn(Optional.of(publicNoticeDate));

    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(context);

    var expectedPublicNoticeView = PublicNoticeTestUtil.createPublishedPublicNoticeView(
        publishedPublicNotice, publicNoticeDate, publishedPublicNoticeRequest);

    assertThat(allPublicNoticesView.getCurrentPublicNotice().getPublicNoticeEvents()).usingRecursiveComparison()
        .isEqualTo(expectedPublicNoticeView.getPublicNoticeEvents());
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
  public void getPublicNoticeDocumentFileViewIfExists_documentLinkExists() {

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

    var actualFileView = publicNoticeService.getLatestPublicNoticeDocumentFileViewIfExists(pwaApplication);
    assertThat(actualFileView).isEqualTo(Optional.of(documentFileView));
  }

  @Test
  public void getPublicNoticeDocumentFileViewIfExists_documentLinkDoesNotExist() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(
        Optional.of(publicNotice));

    when(publicNoticeDocumentRepository.findByPublicNoticeAndDocumentType(publicNotice, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT))
        .thenReturn(Optional.empty());

    var fileView = publicNoticeService.getLatestPublicNoticeDocumentFileViewIfExists(pwaApplication);
    assertThat(fileView).isEqualTo(Optional.empty());
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
