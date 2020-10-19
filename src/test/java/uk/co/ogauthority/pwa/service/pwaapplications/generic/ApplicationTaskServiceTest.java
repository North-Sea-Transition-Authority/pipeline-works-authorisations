package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTaskServiceTest {

  private final ApplicationTask DEFAULT_APP_TASK = ApplicationTask.FIELD_INFORMATION;

  @Mock
  private ApplicationContext springApplicationContext;

  @Mock
  private ApplicationFormSectionService applicationFormSectionService;

  private PwaApplicationDetail pwaApplicationDetail;
  private PwaApplication pwaApplication;

  private ApplicationTaskService applicationTaskService;

  @Before
  public void setup() {

    when(springApplicationContext.getBean(any(Class.class))).thenAnswer(invocation -> {
      Class clazz = invocation.getArgument(0);
      if (ApplicationFormSectionService.class.isAssignableFrom(clazz)) {
        return applicationFormSectionService;
      } else {
        return mock(clazz);
      }
    });

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();

    applicationTaskService = new ApplicationTaskService(springApplicationContext);

  }


  @Test
  public void canShowTask_byAppType_whenAllConditionalTasksShown() {

    when(applicationFormSectionService.canShowInTaskList(eq(pwaApplicationDetail))).thenReturn(true);

    PwaApplicationType.stream().forEach(appType -> {
      pwaApplication.setApplicationType(appType);
      ApplicationTask.stream().forEach(applicationTask -> {
        try {
          var appTypeTasks = getAllExpectedApplicationTasksForApp(appType);
          if (appTypeTasks.contains(applicationTask)) {
            assertThat(applicationTaskService.canShowTask(applicationTask, pwaApplicationDetail)).isTrue();
          } else {
            assertThat(applicationTaskService.canShowTask(applicationTask, pwaApplicationDetail)).isFalse();
          }
        } catch (AssertionError e) {
          throw new AssertionError(
              "Failed at type: " + appType + " and task: " + applicationTask + "\n" + e.getMessage(), e);
        }
      });
    });
  }

  @Test
  public void isTaskComplete_serviceInteractions() {

    when(applicationFormSectionService.isComplete(any())).thenReturn(true);

    assertThat(applicationTaskService.isTaskComplete(DEFAULT_APP_TASK, pwaApplicationDetail)).isTrue();

    verify(applicationFormSectionService, times(1)).isComplete(pwaApplicationDetail);

  }

  @Test
  public void getTaskInfoList_serviceInteractions() {

    var taskInfo = new TaskInfo("INFO", 1L);

    when(applicationFormSectionService.getTaskInfoList(any())).thenReturn(List.of(taskInfo));

    assertThat(applicationTaskService.getTaskInfoList(DEFAULT_APP_TASK, pwaApplicationDetail)).containsExactly(taskInfo);

    verify(applicationFormSectionService, times(1)).getTaskInfoList(pwaApplicationDetail);

  }

  private Set<ApplicationTask> getAllExpectedApplicationTasksForApp(PwaApplicationType appType) {
    switch (appType) {
      case INITIAL:
      case CAT_1_VARIATION:
        return EnumSet.of(
            ApplicationTask.FIELD_INFORMATION,
            ApplicationTask.APPLICATION_USERS,
            ApplicationTask.PROJECT_INFORMATION,
            ApplicationTask.FAST_TRACK,
            ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING,
            ApplicationTask.CROSSING_AGREEMENTS,
            ApplicationTask.LOCATION_DETAILS,
            ApplicationTask.HUOO,
            ApplicationTask.TECHNICAL_DRAWINGS,
            ApplicationTask.PIPELINES,
            ApplicationTask.PIPELINES_HUOO,
            ApplicationTask.CAMPAIGN_WORKS,
            ApplicationTask.PERMANENT_DEPOSITS,
            ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS,
            ApplicationTask.GENERAL_TECH_DETAILS,
            ApplicationTask.FLUID_COMPOSITION,
            ApplicationTask.PIPELINE_OTHER_PROPERTIES,
            ApplicationTask.DESIGN_OP_CONDITIONS,
            ApplicationTask.PARTNER_LETTERS
        );
      case DEPOSIT_CONSENT:
        return EnumSet.of(
            ApplicationTask.FIELD_INFORMATION,
            ApplicationTask.APPLICATION_USERS,
            ApplicationTask.PROJECT_INFORMATION,
            ApplicationTask.FAST_TRACK,
            ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING,
            ApplicationTask.CROSSING_AGREEMENTS,
            ApplicationTask.LOCATION_DETAILS,
            ApplicationTask.HUOO,
            ApplicationTask.PERMANENT_DEPOSITS,
            ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS
        );
      case DECOMMISSIONING:
        return EnumSet.of(
            ApplicationTask.FIELD_INFORMATION,
            ApplicationTask.APPLICATION_USERS,
            ApplicationTask.PROJECT_INFORMATION,
            ApplicationTask.FAST_TRACK,
            ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING,
            ApplicationTask.CROSSING_AGREEMENTS,
            ApplicationTask.LOCATION_DETAILS,
            ApplicationTask.HUOO,
            ApplicationTask.TECHNICAL_DRAWINGS,
            ApplicationTask.PIPELINES,
            ApplicationTask.PIPELINES_HUOO,
            ApplicationTask.CAMPAIGN_WORKS,
            ApplicationTask.PERMANENT_DEPOSITS,
            ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS,
            ApplicationTask.PARTNER_LETTERS
        );
      case OPTIONS_VARIATION:
        return EnumSet.of(
            ApplicationTask.FIELD_INFORMATION,
            ApplicationTask.APPLICATION_USERS,
            ApplicationTask.PROJECT_INFORMATION,
            ApplicationTask.FAST_TRACK,
            ApplicationTask.OPTIONS_TEMPLATE,
            ApplicationTask.SUPPLEMENTARY_DOCUMENTS
        );
      case CAT_2_VARIATION:
        return EnumSet.of(
            ApplicationTask.FIELD_INFORMATION,
            ApplicationTask.APPLICATION_USERS,
            ApplicationTask.PROJECT_INFORMATION,
            ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING,
            ApplicationTask.FAST_TRACK,
            ApplicationTask.CROSSING_AGREEMENTS,
            ApplicationTask.LOCATION_DETAILS,
            ApplicationTask.HUOO,
            ApplicationTask.PIPELINES,
            ApplicationTask.PIPELINES_HUOO,
            ApplicationTask.CAMPAIGN_WORKS,
            ApplicationTask.TECHNICAL_DRAWINGS,
            ApplicationTask.PERMANENT_DEPOSITS,
            ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS,
            ApplicationTask.PARTNER_LETTERS
        );
      case HUOO_VARIATION:
        return EnumSet.of(
            ApplicationTask.FIELD_INFORMATION,
            ApplicationTask.APPLICATION_USERS,
            ApplicationTask.PROJECT_INFORMATION,
            ApplicationTask.FAST_TRACK,
            ApplicationTask.HUOO,
            ApplicationTask.PIPELINES_HUOO
        );
      default:
        return Set.of();
    }
  }

  @Test
  public void copyApplicationTaskDataToApplicationDetail_serviceInteractions() {

    var newDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL
        , pwaApplicationDetail.getMasterPwaApplicationId()
        , pwaApplicationDetail.getId() + 1);

    applicationTaskService.copyApplicationTaskDataToApplicationDetail(DEFAULT_APP_TASK, pwaApplicationDetail, newDetail);

    verify(applicationFormSectionService, times(1)).copySectionInformation(pwaApplicationDetail, newDetail);

  }
}