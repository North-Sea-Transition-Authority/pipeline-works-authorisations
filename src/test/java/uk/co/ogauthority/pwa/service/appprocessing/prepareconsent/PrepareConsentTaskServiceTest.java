package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.options.OptionsApprovalStatus;
import uk.co.ogauthority.pwa.service.documents.DocumentService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PrepareConsentTaskServiceTest {

  @Mock
  private DocumentService documentService;

  @Mock
  private ApproveOptionsService approveOptionsService;

  @Mock
  private ConsentReviewService consentReviewService;

  private PrepareConsentTaskService prepareConsentTaskService;

  @Before
  public void setUp() {
    prepareConsentTaskService = new PrepareConsentTaskService(documentService, approveOptionsService, consentReviewService);
  }

  @Test
  public void canShowInTaskList_editConsentDocumentPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT), null, null,
        Set.of());

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_caseManagementIndustryPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null,
        Set.of());

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermissions_false() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.DRAFT);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null, Set.of());

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_invalidAppStatusAndPermissions_false() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.DRAFT);
    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null, Set.of());

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_completedAppStatus_invalidPermissions_true() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.COMPLETE);
    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null, Set.of());

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void getTaskListEntry_noSatisfactoryVersions() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication()), Set.of());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.CANNOT_START_YET));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_optionsVariation_withSatisfactoryVersions_andApprovedOptionNotConfirmed() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_UNRESPONDED);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_REQUIRED));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_optionsVariation_withSatisfactoryVersions_andApprovedOptionConfirmed() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_CONSENTED_OPTION_CONFIRMED);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    when(documentService.getDocumentInstance(any(), eq(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT))).thenReturn(Optional.empty());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getRoute(processingContext));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_noDocumentLoaded() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    when(documentService.getDocumentInstance(any(), eq(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT))).thenReturn(Optional.empty());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getRoute(processingContext));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_consentReviewer_noOpenReview() {

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
  public void getTaskListEntry_consentReviewer_openReview() {

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
  public void getTaskListEntry_caseofficer_openReview() {

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
  public void getTaskListEntry_documentLoaded() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    when(documentService.getDocumentInstance(any(), eq(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT)))
        .thenReturn(Optional.of(new DocumentInstance()));

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.IN_PROGRESS));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getRoute(processingContext));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }


  @Test
  public void getTaskListEntry_applicationIsConsented_taskCompleted() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.COMPLETE);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    when(consentReviewService.isApplicationConsented(processingContext.getApplicationDetail()))
        .thenReturn(true);

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.COMPLETED));
  }


  @Test
  public void taskAccessible_SatisfactoryVersions_notOption() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isTrue();

  }

  @Test
  public void taskAccessible_SatisfactoryVersions_notOption_userIndustryOnly() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(detail.getPwaApplication(), EnumSet.allOf(PwaOrganisationRole.class)),
        Set.of());
    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  public void taskAccessible_applicationIsCompleted_taskNotAccessible() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.COMPLETE);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(detail.getPwaApplication()), Set.of());

    when(consentReviewService.isApplicationConsented(processingContext.getApplicationDetail())).thenReturn(true);

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  public void taskAccessible_SatisfactoryVersions_userHolder_andCaseOfficer() {

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
  public void taskAccessible_SatisfactoryVersions_consentReviewPermission_openReview() {

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
  public void taskAccessible_SatisfactoryVersions_consentReviewPermission_noReview() {

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
  public void taskAccessible_noSatisfactoryVersions_notOption() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication()), Set.of());

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  public void taskAccessible_optionsVariation_withSatisfactoryVersions_andApprovedOptionNotConfirmed() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_UNRESPONDED);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isFalse();


  }

  @Test
  public void taskAccessible_optionsVariation_withSatisfactoryVersions_andApprovedOptionConfirmed() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_CONSENTED_OPTION_CONFIRMED);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()), Set.of());

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isTrue();

  }


}
