package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;

@RunWith(MockitoJUnitRunner.class)
public class TaskListServiceTest {

  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  @Mock
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private PadFastTrackService padFastTrackService;

  @Mock
  private TaskCompletionService taskCompletionService;

  private TaskListService taskListService;

  @Before
  public void setUp() {
    taskListService = new TaskListService(pwaApplicationRedirectService, applicationBreadcrumbService,
        padFastTrackService, taskCompletionService);
  }

  @Test
  public void pwaInfoTasks_initial() {

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    pwaApplication.setApplicationType(PwaApplicationType.INITIAL);

    assertThat(taskListService.getPwaInfoTasks(pwaApplication)).containsOnlyKeys("Field information");

  }

  @Test
  public void pwaInfoTasks_notInitial() {

    var pwaApplication = new PwaApplication();

    PwaApplicationType.stream().forEach(appType -> {

      if(appType != PwaApplicationType.INITIAL) {

        pwaApplication.setApplicationType(appType);

        assertThat(taskListService.getPwaInfoTasks(pwaApplication)).containsOnlyKeys("No tasks");

      }

    });

  }

  @Test
  public void appInfoTasks_generic() {

    var pwaApplication = new PwaApplication();

    PwaApplicationType.stream().forEach(applicationType -> {

      pwaApplication.setApplicationType(applicationType);

      assertThat(taskListService.getAppInfoTasks(pwaApplication)).containsOnlyKeys(
          "Application contacts",
          "Holders, users, operators, and owners");

    });

  }

  @Test
  public void prepareAppTasks_generic() {

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    var detail = new PwaApplicationDetail();
    detail.setPwaApplication(pwaApplication);

    PwaApplicationType.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getPrepareAppTasks(detail));

        switch(appType) {
          case INITIAL:
          case CAT_1_VARIATION:
            assertThat(taskNamesList).containsOnly(
                "Project information",
                "Environmental and decommissioning",
                "Crossing agreements",
                "Location details",
                ApplicationTask.TECHNICAL_DRAWINGS.getDisplayName()
            );
            break;
          case DEPOSIT_CONSENT:
            assertThat(taskNamesList).containsOnly(
              "Project information",
              "Environmental and decommissioning",
              "Crossing agreements",
              "Location details"
            );
            break;
          case DECOMMISSIONING:
          case OPTIONS_VARIATION:
          assertThat(taskNamesList).containsOnly(
              "Project information",
              "Environmental and decommissioning",
              "Location details"
          );
          break;
        case CAT_2_VARIATION:
          assertThat(taskNamesList).containsOnly(
              "Project information",
              "Crossing agreements",
              "Location details",
              ApplicationTask.TECHNICAL_DRAWINGS.getDisplayName()
            );
            break;
          case HUOO_VARIATION:
            assertThat(taskNamesList).containsOnly("No tasks");
            break;
        }

      } catch (AssertionError e){
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void prepareAppTasks_fastTrackInList() {
    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    var detail = new PwaApplicationDetail();
    detail.setPwaApplication(pwaApplication);

    when(padFastTrackService.isFastTrackRequired(detail)).thenReturn(true);

    PwaApplicationType.stream().forEach(applicationType -> {
      pwaApplication.setApplicationType(applicationType);
      var result = getKeysFromTaskList(taskListService.getPrepareAppTasks(detail));
      assertThat(result).contains("Fast-track");
    });
  }

  @Test
  public void prepareAppTasks_fastTrackNotInList() {
    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    var detail = new PwaApplicationDetail();
    detail.setPwaApplication(pwaApplication);

    when(padFastTrackService.isFastTrackRequired(detail)).thenReturn(false);

    PwaApplicationType.stream().forEach(applicationType -> {
      pwaApplication.setApplicationType(applicationType);
      var result = getKeysFromTaskList(taskListService.getPrepareAppTasks(detail));
      assertThat(result).doesNotContain("Fast-track");
    });
  }

  @Test
  public void getTaskListTemplatePath() {

    PwaApplicationType.stream().forEach(applicationType -> {

      switch (applicationType) {
        case INITIAL:
          assertThat(taskListService.getTaskListTemplatePath(applicationType)).isEqualTo("pwaApplication/initial/initialTaskList");
          break;
        case CAT_1_VARIATION:
          assertThat(taskListService.getTaskListTemplatePath(applicationType)).isEqualTo("pwaApplication/category1/cat1TaskList");
          break;
        case CAT_2_VARIATION:
          assertThat(taskListService.getTaskListTemplatePath(applicationType)).isEqualTo("pwaApplication/category2/cat2TaskList");
          break;
        case HUOO_VARIATION:
          assertThat(taskListService.getTaskListTemplatePath(applicationType)).isEqualTo("pwaApplication/huooVariation/huooTaskList");
          break;
        case DEPOSIT_CONSENT:
          assertThat(taskListService.getTaskListTemplatePath(applicationType)).isEqualTo("pwaApplication/depositConsent/depositConsentTaskList");
          break;
        case DECOMMISSIONING:
          assertThat(taskListService.getTaskListTemplatePath(applicationType)).isEqualTo("pwaApplication/decommissioning/decommissioningTaskList");
          break;
        case OPTIONS_VARIATION:
          assertThat(taskListService.getTaskListTemplatePath(applicationType)).isEqualTo("pwaApplication/optionsVariation/optionsVariationTaskList");
          break;
        default:
          throw new AssertionError();
      }

    });

  }

  @Test
  public void getTaskListModelAndView_generic() {

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    var detail = new PwaApplicationDetail();
    detail.setPwaApplication(pwaApplication);

    PwaApplicationType.stream().forEach(applicationType -> {

      pwaApplication.setApplicationType(applicationType);

      var modelAndView = taskListService.getTaskListModelAndView(detail);

      assertThat(modelAndView.getViewName()).isEqualTo(taskListService.getTaskListTemplatePath(applicationType));

      assertThat(modelAndView.getModel().get("pwaInfoTasks")).isNotNull();
      assertThat(modelAndView.getModel().get("appInfoTasks")).isNotNull();
      assertThat(modelAndView.getModel().get("prepareAppTasks")).isNotNull();

      // TODO: PWA-361 - Remove hard-coded "PWA-Example-BP-2".
      if(applicationType != PwaApplicationType.INITIAL) {
        assertThat(modelAndView.getModel().get("masterPwaReference")).isEqualTo("PWA-Example-BP-2");
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

}
