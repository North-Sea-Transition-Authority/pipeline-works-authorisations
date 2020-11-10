package uk.co.ogauthority.pwa.service.appprocessing.decision;

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
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.documents.DocumentService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationDecisionServiceTest {

  @Mock
  private DocumentService documentService;

  private ApplicationDecisionService applicationDecisionService;

  @Before
  public void setUp() {
    applicationDecisionService = new ApplicationDecisionService(documentService);
  }

  @Test
  public void canShowInTaskList_editConsentDocumentPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT), null, null);

    boolean canShow = applicationDecisionService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_caseManagementIndustryPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null);

    boolean canShow = applicationDecisionService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermissions_false() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null);

    boolean canShow = applicationDecisionService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry_noDocumentLoaded() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    when(documentService.getDocumentInstance(any(), eq(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT))).thenReturn(Optional.empty());

    var taskListEntry = applicationDecisionService.getTaskListEntry(PwaAppProcessingTask.DECISION, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.DECISION.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.DECISION.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskStatus.NOT_STARTED);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_documentLoaded() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    when(documentService.getDocumentInstance(any(), eq(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT)))
        .thenReturn(Optional.of(new DocumentInstance()));

    var taskListEntry = applicationDecisionService.getTaskListEntry(PwaAppProcessingTask.DECISION, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.DECISION.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.DECISION.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskStatus.IN_PROGRESS);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

}
