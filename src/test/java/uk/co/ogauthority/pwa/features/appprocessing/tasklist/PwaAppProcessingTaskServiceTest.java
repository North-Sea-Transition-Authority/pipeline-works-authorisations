package uk.co.ogauthority.pwa.features.appprocessing.tasklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PwaAppProcessingTaskServiceTest {

  @Mock
  private ApplicationContext springApplicationContext;

  @Mock
  private AppProcessingService appProcessingService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PwaAppProcessingContext processingContext;

  private PwaAppProcessingTaskService processingTaskService;

  @BeforeEach
  void setup() {

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
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("group", new ConsultationRequest()),
        Set.of());

    processingTaskService = new PwaAppProcessingTaskService(springApplicationContext);

  }

  @Test
  void canShowTask() {

    PwaAppProcessingTask.stream().forEach(task -> {
      when(processingTaskService.canShowTask(task, processingContext)).thenReturn(true);
      assertThat(processingTaskService.canShowTask(task, processingContext)).isTrue();
    });

  }

  @Test
  void getTaskListEntry() {

    PwaAppProcessingTask.stream().forEach(task -> {

      when(processingTaskService.getTaskListEntry(task, processingContext)).thenCallRealMethod();

      var taskListEntry = processingTaskService.getTaskListEntry(task, processingContext);

      assertThat(taskListEntry.getTaskName()).isEqualTo(task.getTaskName());
      assertThat(taskListEntry.getRoute()).isEqualTo(task.getRoute(processingContext));

    });

  }

}