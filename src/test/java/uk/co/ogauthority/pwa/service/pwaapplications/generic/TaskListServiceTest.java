package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
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

  private TaskListService taskListService;

  @Before
  public void setUp() {
    taskListService = new TaskListService(pwaApplicationRedirectService, applicationBreadcrumbService,
        pwaApplicationDetailService, padFastTrackService);
  }

  @Test
  public void pwaInfoTasks_initial() {

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    pwaApplication.setApplicationType(PwaApplicationType.INITIAL);

    assertThat(taskListService.getPwaInfoTasks(pwaApplication)).containsOnlyKeys(
        "Consent holder", "Field information");

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

      assertThat(taskListService.getAppInfoTasks(pwaApplication)).containsOnlyKeys("Application contacts");

    });

  }

  @Test
  public void prepareAppTasks_generic() {

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);

    PwaApplicationType.stream().forEach(appType -> {

      pwaApplication.setApplicationType(appType);

      switch(appType) {
        case INITIAL:
        case CAT_1_VARIATION:
        case OPTIONS_VARIATION:
        case DECOMMISSIONING:
        case DEPOSIT_CONSENT:
          assertThat(taskListService.getPrepareAppTasks(pwaApplication)).containsOnlyKeys(
              "Project information",
              "Environmental and decommissioning"
          );
          break;
        case CAT_2_VARIATION:
          assertThat(taskListService.getPrepareAppTasks(pwaApplication)).containsOnlyKeys("Project information");
          break;
        case HUOO_VARIATION:
          assertThat(taskListService.getPrepareAppTasks(pwaApplication)).containsOnlyKeys("Project information"); // TODO PWA-66 fix restriction, HUOO shouldn't have this
          break;
      }

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

    PwaApplicationType.stream().forEach(applicationType -> {

      pwaApplication.setApplicationType(applicationType);

      var modelAndView = taskListService.getTaskListModelAndView(pwaApplication);

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

}
