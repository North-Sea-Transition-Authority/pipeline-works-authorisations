package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaView;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaViewService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class TaskListServiceTest {

  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  @Mock
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @Mock
  private TaskCompletionService taskCompletionService;

  @Mock
  protected ApplicationContext springApplicationContext;

  @Mock
  private ApplicationFormSectionService applicationFormSectionService;

  @Mock
  private MasterPwaViewService masterPwaViewService;

  private TaskListService taskListService;

  @Before
  public void setUp() {

    when(springApplicationContext.getBean(any(Class.class))).thenAnswer(invocation -> {
      Class clazz = invocation.getArgument(0);
      if (ApplicationFormSectionService.class.isAssignableFrom(clazz)) {
        return applicationFormSectionService;
      } else {
        return mock(clazz);
      }
    });


    taskListService = new TaskListService(
        springApplicationContext,
        pwaApplicationRedirectService,
        applicationBreadcrumbService,
        taskCompletionService,
        masterPwaViewService);
  }


  @Test
  public void getApplicationTasks_appTypeTasks_whenConditionalTasksShown() {

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    var detail = new PwaApplicationDetail();
    detail.setPwaApplication(pwaApplication);

    when(applicationFormSectionService.canShowInTaskList(detail)).thenReturn(true);

    PwaApplicationType.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getApplicationTasks(detail));

        switch (appType) {
          case INITIAL:
          case CAT_1_VARIATION:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.FAST_TRACK.getDisplayName(),
                ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING.getDisplayName(),
                ApplicationTask.CROSSING_AGREEMENTS.getDisplayName(),
                ApplicationTask.LOCATION_DETAILS.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName(),
                ApplicationTask.TECHNICAL_DRAWINGS.getDisplayName(),
                ApplicationTask.PIPELINES.getDisplayName(),
                ApplicationTask.PIPELINES_HUOO.getDisplayName(),
                ApplicationTask.CAMPAIGN_WORKS.getDisplayName(),
                ApplicationTask.PERMANENT_DEPOSITS.getDisplayName(),
                ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS.getDisplayName(),
                ApplicationTask.GENERAL_TECH_DETAILS.getDisplayName(),
                ApplicationTask.FLUID_COMPOSITION.getDisplayName(),
                ApplicationTask.PIPELINE_OTHER_PROPERTIES.getDisplayName(),
                ApplicationTask.DESIGN_OP_CONDITIONS.getDisplayName(),
                ApplicationTask.PARTNER_LETTERS.getDisplayName()
            );
            break;
          case DEPOSIT_CONSENT:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.FAST_TRACK.getDisplayName(),
                ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING.getDisplayName(),
                ApplicationTask.CROSSING_AGREEMENTS.getDisplayName(),
                ApplicationTask.LOCATION_DETAILS.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName(),
                ApplicationTask.PERMANENT_DEPOSITS.getDisplayName(),
                ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS.getDisplayName()
            );
            break;
          case DECOMMISSIONING:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.FAST_TRACK.getDisplayName(),
                ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING.getDisplayName(),
                ApplicationTask.CROSSING_AGREEMENTS.getDisplayName(),
                ApplicationTask.LOCATION_DETAILS.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName(),
                ApplicationTask.TECHNICAL_DRAWINGS.getDisplayName(),
                ApplicationTask.PIPELINES.getDisplayName(),
                ApplicationTask.PIPELINES_HUOO.getDisplayName(),
                ApplicationTask.CAMPAIGN_WORKS.getDisplayName(),
                ApplicationTask.PERMANENT_DEPOSITS.getDisplayName(),
                ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS.getDisplayName(),
                ApplicationTask.PARTNER_LETTERS.getDisplayName()
            );
            break;
          case OPTIONS_VARIATION:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.FAST_TRACK.getDisplayName(),
                ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING.getDisplayName(),
                ApplicationTask.LOCATION_DETAILS.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName(),
                ApplicationTask.PERMANENT_DEPOSITS.getDisplayName(),
                ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS.getDisplayName(),
                ApplicationTask.PARTNER_LETTERS.getDisplayName()
            );
            break;
          case CAT_2_VARIATION:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.FAST_TRACK.getDisplayName(),
                ApplicationTask.CROSSING_AGREEMENTS.getDisplayName(),
                ApplicationTask.LOCATION_DETAILS.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName(),
                ApplicationTask.PIPELINES.getDisplayName(),
                ApplicationTask.PIPELINES_HUOO.getDisplayName(),
                ApplicationTask.CAMPAIGN_WORKS.getDisplayName(),
                ApplicationTask.TECHNICAL_DRAWINGS.getDisplayName(),
                ApplicationTask.PERMANENT_DEPOSITS.getDisplayName(),
                ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS.getDisplayName(),
                ApplicationTask.PARTNER_LETTERS.getDisplayName()
            );
            break;
          case HUOO_VARIATION:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.FAST_TRACK.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName(),
                ApplicationTask.PIPELINES_HUOO.getDisplayName()
            );
            break;
        }

      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void getTaskListModelAndView_generic() {

    var masterPwaView = mock(MasterPwaView.class);
    when(masterPwaView.getReference()).thenReturn("PWA-Example");

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    var detail = new PwaApplicationDetail();
    detail.setPwaApplication(pwaApplication);

    when(masterPwaViewService.getCurrentMasterPwaView(pwaApplication)).thenReturn(masterPwaView);

    PwaApplicationType.stream().forEach(applicationType -> {

      pwaApplication.setApplicationType(applicationType);

      var modelAndView = taskListService.getTaskListModelAndView(detail);

      assertThat(modelAndView.getViewName()).isEqualTo(TaskListService.TASK_LIST_TEMPLATE_PATH);

      assertThat(modelAndView.getModel().get("pwaInfoTasks")).isNotNull();
      assertThat(modelAndView.getModel().get("appInfoTasks")).isNotNull();
      assertThat(modelAndView.getModel().get("prepareAppTasks")).isNotNull();

      if (applicationType != PwaApplicationType.INITIAL) {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isEqualTo("PWA-Example");
      } else {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isNull();
      }

      verify(applicationBreadcrumbService, times(1)).fromWorkArea(modelAndView, "Task list");

    });

  }

  private List<String> getKeysFromTaskList(List<TaskListEntry> taskList) {
    return taskList.stream()
        .map(TaskListEntry::getTaskName)
        .collect(Collectors.toList());
  }

  @Test
  public void anyTaskShownForApplication_callsOutToRequestedTaskServices_whenMultiple_andNeitherShown() {

    var projTask = ApplicationTask.PROJECT_INFORMATION;
    var permTask = ApplicationTask.PERMANENT_DEPOSITS;
    ApplicationFormSectionService projService = mock(ApplicationFormSectionService.class);
    ApplicationFormSectionService permService = mock(ApplicationFormSectionService.class);
    when(springApplicationContext.getBean(projTask.getServiceClass())).thenAnswer(invocation -> projService);
    when(springApplicationContext.getBean(permTask.getServiceClass())).thenAnswer(invocation -> permService);

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var result = taskListService.anyTaskShownForApplication(
        Set.of(projTask, permTask),
        pwaApplicationDetail
    );

    verify(projService, times(1)).canShowInTaskList(pwaApplicationDetail);
    verify(permService, times(1)).canShowInTaskList(pwaApplicationDetail);
    assertThat(result).isFalse();

  }

  @Test
  public void anyTaskShownForApplication_callsOutToRequestedTaskServices_whenMultiple_andOnlyOneShown() {

    var projTask = ApplicationTask.PROJECT_INFORMATION;
    var permTask = ApplicationTask.PERMANENT_DEPOSITS;
    ApplicationFormSectionService projService = mock(ApplicationFormSectionService.class);
    ApplicationFormSectionService permService = mock(ApplicationFormSectionService.class);
    when(springApplicationContext.getBean(projTask.getServiceClass())).thenAnswer(invocation -> projService);
    when(springApplicationContext.getBean(permTask.getServiceClass())).thenAnswer(invocation -> permService);

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(projService.canShowInTaskList(pwaApplicationDetail)).thenReturn(true);

    var result = taskListService.anyTaskShownForApplication(
        Set.of(projTask, permTask),
        pwaApplicationDetail
    );

    verify(projService, times(1)).canShowInTaskList(pwaApplicationDetail);
    verify(permService, times(1)).canShowInTaskList(pwaApplicationDetail);
    assertThat(result).isTrue();

  }

}
