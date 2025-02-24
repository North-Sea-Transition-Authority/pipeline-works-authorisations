package uk.co.ogauthority.pwa.features.appprocessing.processingwarnings;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentDocumentUrlProvider;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.ConsultationService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

@ExtendWith(MockitoExtension.class)
class AppProcessingTaskWarningServiceTest {

  @Mock
  private ConsultationService consultationService;

  @Mock
  private PublicNoticeService publicNoticeService;

  private AppProcessingTaskWarningService appProcessingTaskWarningService;

  private PwaApplication application;


  @BeforeEach
  void setUp() throws Exception {

    appProcessingTaskWarningService = new AppProcessingTaskWarningService(consultationService, publicNoticeService);
    application = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();
  }


  @Test
  void getNonBlockingTasksWarning_tasksNotRequired_noWarnings() {

    when(consultationService.consultationsTaskRequired(application)).thenReturn(false);
    when(publicNoticeService.publicNoticeTaskRequired(application)).thenReturn(false);

    var nonBlockingWarning = appProcessingTaskWarningService.getNonBlockingTasksWarning(
        application, NonBlockingWarningPage.ISSUE_CONSENT);

    assertThat(nonBlockingWarning.getTasksHaveWarnings()).isFalse();
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).isNull();
    assertThat(nonBlockingWarning.getReturnMessage()).isNull();
  }

  @Test
  void getNonBlockingTasksWarning_tasksRequired_tasksNotStarted_warningMessageIncludesBothTasks() {

    when(consultationService.consultationsTaskRequired(application)).thenReturn(true);
    when(consultationService.getTaskStatus(application)).thenReturn(TaskStatus.NOT_STARTED);

    when(publicNoticeService.publicNoticeTaskRequired(application)).thenReturn(true);
    when(publicNoticeService.publicNoticeTaskStarted(application)).thenReturn(false);

    var nonBlockingWarning = appProcessingTaskWarningService.getNonBlockingTasksWarning(
        application, NonBlockingWarningPage.ISSUE_CONSENT);

    assertThat(nonBlockingWarning.getTasksHaveWarnings()).isTrue();
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).contains("consultations");
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).contains("public notice");
  }

  @Test
  void getNonBlockingTasksWarning_tasksRequired_consultationsNotStarted_publicNoticeStarted_warningMessageIncludesRelevantTask() {

    when(consultationService.consultationsTaskRequired(application)).thenReturn(true);
    when(consultationService.getTaskStatus(application)).thenReturn(TaskStatus.NOT_STARTED);

    when(publicNoticeService.publicNoticeTaskRequired(application)).thenReturn(true);
    when(publicNoticeService.publicNoticeTaskStarted(application)).thenReturn(true);

    var nonBlockingWarning = appProcessingTaskWarningService.getNonBlockingTasksWarning(
        application, NonBlockingWarningPage.ISSUE_CONSENT);

    assertThat(nonBlockingWarning.getTasksHaveWarnings()).isTrue();
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).contains("consultations");
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).doesNotContain("public notice");
  }

  @Test
  void getNonBlockingTasksWarning_tasksRequired_consultationsStarted_publicNoticeNotStarted_warningMessageIncludesRelevantTask() {

    when(consultationService.consultationsTaskRequired(application)).thenReturn(true);
    when(consultationService.getTaskStatus(application)).thenReturn(TaskStatus.COMPLETED);

    when(publicNoticeService.publicNoticeTaskRequired(application)).thenReturn(true);
    when(publicNoticeService.publicNoticeTaskStarted(application)).thenReturn(false);

    var nonBlockingWarning = appProcessingTaskWarningService.getNonBlockingTasksWarning(
        application, NonBlockingWarningPage.ISSUE_CONSENT);

    assertThat(nonBlockingWarning.getTasksHaveWarnings()).isTrue();
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).doesNotContain("consultations");
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).contains("public notice");
  }

  @Test
  void getNonBlockingTasksWarning_warningAtSendForApprovalStage_returnMessageLinksToCaseManagement() {

    when(consultationService.consultationsTaskRequired(application)).thenReturn(true);
    when(consultationService.getTaskStatus(application)).thenReturn(TaskStatus.NOT_STARTED);

    var nonBlockingWarning = appProcessingTaskWarningService.getNonBlockingTasksWarning(
        application, NonBlockingWarningPage.SEND_FOR_APPROVAL);

    assertThat(nonBlockingWarning.getReturnMessage()).isNotNull();
    assertThat(nonBlockingWarning.getReturnMessage().getMessagePrefix()).contains("send for approval");
    assertThat(nonBlockingWarning.getReturnMessage().getUrlLinkText()).isEqualTo("case management");
    assertThat(nonBlockingWarning.getReturnMessage().getReturnUrl()).isEqualTo(CaseManagementUtils.routeCaseManagement(application));
    assertThat(nonBlockingWarning.getReturnMessage().getMessageSuffix()).isNotNull();
  }

  @Test
  void getNonBlockingTasksWarning_warningAtIssueConsentStage_returnMessageLinksToReturnToCaseOfficer() {

    when(consultationService.consultationsTaskRequired(application)).thenReturn(true);
    when(consultationService.getTaskStatus(application)).thenReturn(TaskStatus.NOT_STARTED);

    var nonBlockingWarning = appProcessingTaskWarningService.getNonBlockingTasksWarning(
        application, NonBlockingWarningPage.ISSUE_CONSENT);

    assertThat(nonBlockingWarning.getReturnMessage()).isNotNull();
    assertThat(nonBlockingWarning.getReturnMessage().getMessagePrefix()).contains("issue");
    assertThat(nonBlockingWarning.getReturnMessage().getMessagePrefix()).contains("consent");
    assertThat(nonBlockingWarning.getReturnMessage().getUrlLinkText()).isEqualTo("return to case officer");
    var urlProvider = new ConsentDocumentUrlProvider(application);
    assertThat(nonBlockingWarning.getReturnMessage().getReturnUrl()).isEqualTo(urlProvider.getReturnToCaseOfficerUrl());
    assertThat(nonBlockingWarning.getReturnMessage().getMessageSuffix()).isNull();
  }




}