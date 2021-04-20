package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.options.OptionsApprovalStatus;
import uk.co.ogauthority.pwa.service.documents.DocumentService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PrepareConsentTaskServiceTest {

  @Mock
  private DocumentService documentService;

  @Mock
  private ApproveOptionsService approveOptionsService;

  private PrepareConsentTaskService prepareConsentTaskService;

  @Before
  public void setUp() {
    prepareConsentTaskService = new PrepareConsentTaskService(documentService, approveOptionsService);
  }

  @Test
  public void canShowInTaskList_editConsentDocumentPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT), null, null);

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_caseManagementIndustryPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null);

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermissions_false() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null);

    boolean canShow = prepareConsentTaskService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry_noSatisfactoryVersions() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication()));

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
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()));

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
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()));

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
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()));

    when(documentService.getDocumentInstance(any(), eq(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT))).thenReturn(Optional.empty());

    var taskListEntry = prepareConsentTaskService.getTaskListEntry(PwaAppProcessingTask.PREPARE_CONSENT, processingContext);

    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.PREPARE_CONSENT.getRoute(processingContext));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_documentLoaded() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()));

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
  public void taskAccessible_SatisfactoryVersions_notOption() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()));

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isTrue();

  }

  @Test
  public void taskAccessible_noSatisfactoryVersions_notOption() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication()));

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  public void taskAccessible_optionsVariation_withSatisfactoryVersions_andApprovedOptionNotConfirmed() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_UNRESPONDED);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()));

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isFalse();


  }

  @Test
  public void taskAccessible_optionsVariation_withSatisfactoryVersions_andApprovedOptionConfirmed() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_CONSENTED_OPTION_CONFIRMED);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()));

    var taskAccessible = prepareConsentTaskService.taskAccessible(processingContext);

    assertThat(taskAccessible).isTrue();

  }



}
