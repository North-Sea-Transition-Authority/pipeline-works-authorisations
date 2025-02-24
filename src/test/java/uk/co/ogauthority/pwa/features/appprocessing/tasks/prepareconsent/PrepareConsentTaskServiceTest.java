package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransfer;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.options.OptionsApprovalStatus;
import uk.co.ogauthority.pwa.service.documents.DocumentService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrepareConsentTaskServiceTest {

  @Mock
  private DocumentService documentService;

  @Mock
  private ApproveOptionsService approveOptionsService;

  @Mock
  private ConsentReviewService consentReviewService;

  @Mock
  PadPipelineTransferService pipelineTransferService;

  private PrepareConsentTaskService prepareConsentTaskService;

  @BeforeEach
  void setUp() {

    when(approveOptionsService.getOptionsApprovalStatus(any(PwaApplicationDetail.class))).thenReturn(OptionsApprovalStatus.NOT_APPLICABLE);

    prepareConsentTaskService = new PrepareConsentTaskService(
        documentService,
        approveOptionsService,
        consentReviewService,
        pipelineTransferService);

  }

  @Test
  void canShowInTaskList_editConsentDocumentPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT), null, null,
        Set.of());

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void canShowInTaskList_caseManagementIndustryPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null,
        Set.of());

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void canShowInTaskList_noPermissions_false() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.DRAFT);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null, Set.of());

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  void canShowInTaskList_invalidAppStatusAndPermissions_false() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.DRAFT);
    var processingContext = PwaAppProcessingContextTestUtil.withoutPermissions(detail);

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  void canShowInTaskList_completedAppStatus_invalidPermissions_true() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.COMPLETE);
    var processingContext = PwaAppProcessingContextTestUtil.withoutPermissions(detail);

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void getTaskListEntry_noSatisfactoryVersions() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    var appInvolvementDto = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(detail.getPwaApplication(),
        Set.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
    );

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(PwaAppProcessingPermission.CONSENT_REVIEW),
        null, appInvolvementDto, Set.of());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.CANNOT_START_YET));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_atLeastOneSatisfactoryVersion() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    var appInvolvementDto = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(detail.getPwaApplication(),
        Set.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.CASE_OFFICER_STAGE_AND_USER_ASSIGNED,
              ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION)
    );

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(PwaAppProcessingPermission.CONSENT_REVIEW),
        null, appInvolvementDto, Set.of());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_optionsVariation_withSatisfactoryVersions_notApproved() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.NOT_APPROVED);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        getAppInvolvementWithSatisfactoryVersionsAndAssignedCaseOfficer(detail), Set.of());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_REQUIRED));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_optionsVariation_withSatisfactoryVersions_andOptionsApprovedUnresponded() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_UNRESPONDED);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        getAppInvolvementWithSatisfactoryVersionsAndAssignedCaseOfficer(detail), Set.of());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_REQUIRED));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_optionsVariation_withSatisfactoryVersions_andApprovedOptionNotConfirmed() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_UNRESPONDED);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        getAppInvolvementWithSatisfactoryVersionsAndAssignedCaseOfficer(detail), Set.of());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_REQUIRED));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_optionsVariation_withSatisfactoryVersions_andApprovedOptionNotConfirmed_andPendingTransfer() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_UNRESPONDED);

    var transfer = new PadPipelineTransfer();
    var donorDetail = new PwaApplicationDetail();
    donorDetail.setStatus(PwaApplicationStatus.CONSENT_REVIEW);
    transfer.setDonorApplicationDetail(donorDetail);
    when(pipelineTransferService.findByRecipientApplication(detail)).thenReturn(List.of(transfer));

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        getAppInvolvementWithSatisfactoryVersionsAndAssignedCaseOfficer(detail), Set.of());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.AWAITING_TRANSFER_COMPLETION));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_optionsVariation_withSatisfactoryVersions_andApprovedOptionConfirmed() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_CONSENTED_OPTION_CONFIRMED);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        getAppInvolvementWithSatisfactoryVersionsAndAssignedCaseOfficer(detail), Set.of());

    when(documentService.getDocumentInstance(any(), eq(DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT))).thenReturn(Optional.empty());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getRoute(processingContext));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_noDocumentLoaded() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    when(documentService.getDocumentInstance(any(), eq(DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT))).thenReturn(Optional.empty());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getRoute(processingContext));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_consentReviewer_noOpenReview() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(PwaAppProcessingPermission.CONSENT_REVIEW),
        null,
        ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(detail.getPwaApplication(),
            Set.of(
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION
            )),
        Set.of());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);

  }

  @Test
  void getTaskListEntry_consentReviewer_openReview() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(PwaAppProcessingPermission.CONSENT_REVIEW),
        null,
        ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(detail.getPwaApplication(),
            Set.of(
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION,
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.OPEN_CONSENT_REVIEW
            )),
        Set.of());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);

  }

  @Test
  void getTaskListEntry_caseofficer_openReview() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(PwaAppProcessingPermission.CASE_OFFICER_REVIEW),
        null,
        ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(detail.getPwaApplication(),
            Set.of(
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION,
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.OPEN_CONSENT_REVIEW,
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.CASE_OFFICER_STAGE_AND_USER_ASSIGNED
            )),
        Set.of());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);

  }

  @Test
  void getTaskListEntry_documentLoaded() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    when(documentService.getDocumentInstance(any(), eq(DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT)))
        .thenReturn(Optional.of(new DocumentInstance()));

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.IN_PROGRESS));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getRoute(processingContext));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }


  @Test
  void getTaskListEntry_applicationIsConsented_taskCompleted() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.COMPLETE);

    var processingContext = PwaAppProcessingContextTestUtil.withAppInvolvement(detail,
        ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(detail.getPwaApplication(),
            Set.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION)));

    when(consentReviewService.isApplicationConsented(processingContext.getApplicationDetail()))
        .thenReturn(true);

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.COMPLETED));
  }


  @Test
  void taskAccessible_SatisfactoryVersions_notOption() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isTrue();

  }

  @Test
  void taskAccessible_SatisfactoryVersions_notOption_userIndustryOnly() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(detail.getPwaApplication(), EnumSet.allOf(PwaOrganisationRole.class)),
        Set.of());
    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  void taskAccessible_applicationIsCompleted_taskNotAccessible() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.COMPLETE);

    var processingContext = PwaAppProcessingContextTestUtil.withoutPermissions(detail);

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  void taskAccessible_SatisfactoryVersions_userHolder_andCaseOfficer() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        ApplicationInvolvementDtoTestUtil.generateAppInvolvement(detail.getPwaApplication(),
            Set.of(
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.CASE_OFFICER_STAGE_AND_USER_ASSIGNED,
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION
            ),
            Set.of(),
            EnumSet.allOf(PwaOrganisationRole.class),
            null
        ),
        Set.of());
    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isTrue();

  }

  @Test
  void taskAccessible_satisfactoryVersions_assignedCaseOfficer_noConsentReviewPermission_caseOfficerReviewStage() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(),
        null,
        ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(detail.getPwaApplication(),
            Set.of(
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.CASE_OFFICER_STAGE_AND_USER_ASSIGNED,
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION
            )),
        Set.of());
    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isTrue();

  }

  @Test
  void taskAccessible_satisfactoryVersions_assignedCaseOfficer_consentReviewPermission_caseOfficerReviewStage() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(PwaAppProcessingPermission.CONSENT_REVIEW),
        null,
        ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(detail.getPwaApplication(),
            Set.of(
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.CASE_OFFICER_STAGE_AND_USER_ASSIGNED,
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION
            )),
        Set.of());
    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isTrue();

  }

  @Test
  void taskAccessible_satisfactoryVersions_noConsentReviewPermission_openReview() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.CONSENT_REVIEW);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(),
        null,
        ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(detail.getPwaApplication(),
            Set.of(
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.PWA_MANAGER_STAGE,
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION,
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.OPEN_CONSENT_REVIEW
            )),
        Set.of());
    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  void taskAccessible_SatisfactoryVersions_consentReviewPermission_openReview() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(PwaAppProcessingPermission.CONSENT_REVIEW),
        null,
        ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(detail.getPwaApplication(),
            Set.of(
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.PWA_MANAGER_STAGE,
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION,
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.OPEN_CONSENT_REVIEW
            )),
        Set.of());
    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isTrue();

  }

  @Test
  void taskAccessible_SatisfactoryVersions_notAssignedCaseOfficer_consentReviewPermission_noReview() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(PwaAppProcessingPermission.CONSENT_REVIEW),
        null,
        ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(detail.getPwaApplication(),
            Set.of(
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.PWA_MANAGER_STAGE,
                ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION
            )),
        Set.of());
    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  void taskAccessible_noSatisfactoryVersions_notOption() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication()), Set.of());

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  void taskAccessible_optionsVariation_withSatisfactoryVersions_andApprovedOptionNotConfirmed() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_UNRESPONDED);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isFalse();


  }

  @Test
  void taskAccessible_optionsVariation_withSatisfactoryVersions_andApprovedOptionConfirmed() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_CONSENTED_OPTION_CONFIRMED);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isTrue();

  }

  @Test
  void taskAccessible_transferReleaseNotConsented() {
    var recipientDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    var donorDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    donorDetail.setStatus(PwaApplicationStatus.CONSENT_REVIEW);

    var processingContext = new PwaAppProcessingContext(recipientDetail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(recipientDetail.getPwaApplication()), Set.of());
    var pipelineTransfer = new PadPipelineTransfer()
        .setDonorApplicationDetail(donorDetail)
        .setRecipientApplicationDetail(recipientDetail);
    when(pipelineTransferService.findByRecipientApplication(recipientDetail)).thenReturn(List.of(pipelineTransfer));

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);
    assertThat(taskAccessible).isTrue();
  }

  @Test
  void taskAccessible_transferReleaseConsented() {
    var recipientDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    var donorDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    donorDetail.setStatus(PwaApplicationStatus.COMPLETE);

    var processingContext = new PwaAppProcessingContext(recipientDetail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(recipientDetail.getPwaApplication()), Set.of());
    var pipelineTransfer = new PadPipelineTransfer()
        .setDonorApplicationDetail(donorDetail)
        .setRecipientApplicationDetail(recipientDetail);
    when(pipelineTransferService.findByRecipientApplication(recipientDetail)).thenReturn(List.of(pipelineTransfer));

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);
    assertThat(taskAccessible).isTrue();
  }

  @Test
  void taskStatus_transferReleaseNotConsented() {
    var recipientDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    var donorDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    donorDetail.setStatus(PwaApplicationStatus.CONSENT_REVIEW);

    var processingContext = new PwaAppProcessingContext(recipientDetail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(recipientDetail.getPwaApplication()), Set.of());
    var pipelineTransfer = new PadPipelineTransfer()
        .setDonorApplicationDetail(donorDetail)
        .setRecipientApplicationDetail(recipientDetail);
    when(pipelineTransferService.findByRecipientApplication(recipientDetail)).thenReturn(List.of(pipelineTransfer));

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.AWAITING_TRANSFER_COMPLETION));
  }

  @Test
  void taskStatus_transferReleaseConsented() {
    var recipientDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    var donorDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    donorDetail.setStatus(PwaApplicationStatus.COMPLETE);

    var processingContext = new PwaAppProcessingContext(recipientDetail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(recipientDetail.getPwaApplication()), Set.of());
    var pipelineTransfer = new PadPipelineTransfer()
        .setDonorApplicationDetail(donorDetail)
        .setRecipientApplicationDetail(recipientDetail);
    when(pipelineTransferService.findByRecipientApplication(recipientDetail)).thenReturn(List.of(pipelineTransfer));

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
  }

  @NotNull
  private ApplicationInvolvementDto getAppInvolvementWithSatisfactoryVersionsAndAssignedCaseOfficer(PwaApplicationDetail detail) {
    return ApplicationInvolvementDtoTestUtil.generateAppInvolvement(
        detail.getPwaApplication(),
        EnumSet.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION,
            ApplicationInvolvementDtoTestUtil.InvolvementFlag.CASE_OFFICER_STAGE_AND_USER_ASSIGNED),
        EnumSet.noneOf(PwaContactRole.class),
        EnumSet.noneOf(PwaOrganisationRole.class),
        ConsultationInvolvementDtoTestUtil.emptyConsultationInvolvement()
    );
  }

}
