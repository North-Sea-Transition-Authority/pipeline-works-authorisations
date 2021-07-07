package uk.co.ogauthority.pwa.service.appprocessing.appprocessingwarning;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.ConsultationService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

@RunWith(MockitoJUnitRunner.class)
public class AppProcessingTaskWarningServiceTest {

  @Mock
  private ConsultationService consultationService;

  @Mock
  private PublicNoticeService publicNoticeService;

  private AppProcessingTaskWarningService appProcessingTaskWarningService;

  private PwaApplication application;


  @Before
  public void setUp() throws Exception {

    appProcessingTaskWarningService = new AppProcessingTaskWarningService(consultationService, publicNoticeService);
    application = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();
  }


  @Test
  public void getNonBlockingTasksWarning_tasksNotRequired_noWarnings() {

    when(consultationService.consultationsTaskRequired(application)).thenReturn(false);
    when(publicNoticeService.publicNoticeTaskRequired(application)).thenReturn(false);

    var nonBlockingWarning = appProcessingTaskWarningService.getNonBlockingTasksWarning(application);

    assertThat(nonBlockingWarning.getTasksHaveWarnings()).isFalse();
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).isNull();
  }

  @Test
  public void getNonBlockingTasksWarning_tasksRequired_tasksNotStarted_warningMessageIncludesBothTasks() {

    when(consultationService.consultationsTaskRequired(application)).thenReturn(true);
    when(consultationService.getTaskStatus(application)).thenReturn(TaskStatus.NOT_STARTED);

    when(publicNoticeService.publicNoticeTaskRequired(application)).thenReturn(true);
    when(publicNoticeService.publicNoticeTaskStarted(application)).thenReturn(false);

    var nonBlockingWarning = appProcessingTaskWarningService.getNonBlockingTasksWarning(application);

    assertThat(nonBlockingWarning.getTasksHaveWarnings()).isTrue();
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).contains("consultations");
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).contains("public notice");
    assertThat(nonBlockingWarning.getReturnUrl()).contains(CaseManagementUtils.routeCaseManagement(application));
  }

  @Test
  public void getNonBlockingTasksWarning_tasksRequired_consultationsNotStarted_publicNoticeStarted_warningMessageIncludesRelevantTask() {

    when(consultationService.consultationsTaskRequired(application)).thenReturn(true);
    when(consultationService.getTaskStatus(application)).thenReturn(TaskStatus.NOT_STARTED);

    when(publicNoticeService.publicNoticeTaskRequired(application)).thenReturn(true);
    when(publicNoticeService.publicNoticeTaskStarted(application)).thenReturn(true);

    var nonBlockingWarning = appProcessingTaskWarningService.getNonBlockingTasksWarning(application);

    assertThat(nonBlockingWarning.getTasksHaveWarnings()).isTrue();
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).contains("consultations");
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).doesNotContain("public notice");
  }

  @Test
  public void getNonBlockingTasksWarning_tasksRequired_consultationsStarted_publicNoticeNotStarted_warningMessageIncludesRelevantTask() {

    when(consultationService.consultationsTaskRequired(application)).thenReturn(true);
    when(consultationService.getTaskStatus(application)).thenReturn(TaskStatus.COMPLETED);

    when(publicNoticeService.publicNoticeTaskRequired(application)).thenReturn(true);
    when(publicNoticeService.publicNoticeTaskStarted(application)).thenReturn(false);

    var nonBlockingWarning = appProcessingTaskWarningService.getNonBlockingTasksWarning(application);

    assertThat(nonBlockingWarning.getTasksHaveWarnings()).isTrue();
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).doesNotContain("consultations");
    assertThat(nonBlockingWarning.getIncompleteTasksWarningText()).contains("public notice");
  }




}