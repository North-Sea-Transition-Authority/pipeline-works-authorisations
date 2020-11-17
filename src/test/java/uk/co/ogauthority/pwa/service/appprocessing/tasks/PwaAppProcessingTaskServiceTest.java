package uk.co.ogauthority.pwa.service.appprocessing.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaAppProcessingTaskServiceTest {

  @Mock
  private ApplicationContext springApplicationContext;

  @Mock
  private AppProcessingService appProcessingService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PwaAppProcessingContext processingContext;

  private PwaAppProcessingTaskService processingTaskService;

  @Before
  public void setup() {

    when(springApplicationContext.getBean(any(Class.class))).thenAnswer(invocation -> {
      Class clazz = invocation.getArgument(0);
      if (AppProcessingService.class.isAssignableFrom(clazz)) {
        return appProcessingService;
      } else {
        return mock(clazz);
      }
    });

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    processingContext = new PwaAppProcessingContext(
        pwaApplicationDetail,
        new WebUserAccount(1),
        EnumSet.allOf(PwaAppProcessingPermission.class),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("group", new ConsultationRequest()));

    processingTaskService = new PwaAppProcessingTaskService(springApplicationContext);

  }

  @Test
  public void canShowTask() {

    PwaAppProcessingTask.stream().forEach(task -> {
      when(processingTaskService.canShowTask(task, processingContext)).thenReturn(true);
      assertThat(processingTaskService.canShowTask(task, processingContext)).isTrue();
    });

  }

  @Test
  public void getTaskListEntry() {

    PwaAppProcessingTask.stream().forEach(task -> {

      when(processingTaskService.getTaskListEntry(task, processingContext)).thenCallRealMethod();

      var taskListEntry = processingTaskService.getTaskListEntry(task, processingContext);

      assertThat(taskListEntry.getTaskName()).isEqualTo(task.getTaskName());
      assertThat(taskListEntry.getRoute()).isEqualTo(task.getRoute(processingContext));

    });

  }

}