package uk.co.ogauthority.pwa.integration.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaViewService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskCompletionService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationReferencingService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
// IJ seems to give spurious warnings when running with embedded H2
public class TaskListServiceIntegrationTestTest {

  private TaskListService taskListService;

  @Autowired
  private PwaApplicationCreationService pwaApplicationCreationService;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  private ApplicationBreadcrumbService breadcrumbService;

  @Autowired
  private TaskCompletionService taskCompletionService;

  @Autowired
  private PwaContactService pwaContactService;

  // this needs to be mocked so we dont try increment a sequence that doesnt exist in h2
  @MockBean
  private PwaApplicationReferencingService pwaApplicationReferencingService;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;

  @MockBean
  private CampaignWorksService campaignWorksService;

  @MockBean
  private PadFastTrackService padFastTrackService;

  @MockBean
  private PermanentDepositService permanentDepositService;

  @MockBean
  private MasterPwaViewService masterPwaViewService;

  @Before
  public void setup() {

    // by default, conditional app tasks not shown
    pwaApplicationDetail = pwaApplicationCreationService.createInitialPwaApplication(new WebUserAccount(1));
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    taskListService = new TaskListService(applicationContext,
        pwaApplicationRedirectService,
        breadcrumbService,
        taskCompletionService,
        pwaContactService,
        masterPwaViewService);

  }


  @Test
  public void prepareAppTasks_forEveryAppType_assertAllTasksWithNoCrossSectionDependencies() {

    PwaApplicationType.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getPrepareAppTasks(pwaApplicationDetail));

        switch (appType) {
          case INITIAL:
          case CAT_1_VARIATION:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING.getDisplayName(),
                ApplicationTask.CROSSING_AGREEMENTS.getDisplayName(),
                ApplicationTask.LOCATION_DETAILS.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName(),
                ApplicationTask.TECHNICAL_DRAWINGS.getDisplayName(),
                ApplicationTask.PIPELINES.getDisplayName(),
                ApplicationTask.PIPELINES_HUOO.getDisplayName(),
                ApplicationTask.GENERAL_TECH_DETAILS.getDisplayName(),
                ApplicationTask.FLUID_COMPOSITION.getDisplayName(),
                ApplicationTask.DESIGN_OP_CONDITIONS.getDisplayName()
            );
            break;
          case DEPOSIT_CONSENT:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING.getDisplayName(),
                ApplicationTask.CROSSING_AGREEMENTS.getDisplayName(),
                ApplicationTask.LOCATION_DETAILS.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName()
            );
            break;
          case DECOMMISSIONING:
          case OPTIONS_VARIATION:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING.getDisplayName(),
                ApplicationTask.LOCATION_DETAILS.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName()
            );
            break;
          case CAT_2_VARIATION:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.CROSSING_AGREEMENTS.getDisplayName(),
                ApplicationTask.LOCATION_DETAILS.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName(),
                ApplicationTask.PIPELINES.getDisplayName(),
                ApplicationTask.PIPELINES_HUOO.getDisplayName(),
                ApplicationTask.TECHNICAL_DRAWINGS.getDisplayName()
            );
            break;
          case HUOO_VARIATION:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
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

  private EnumSet<PwaApplicationType> getCampaignWorksAppTypes() {
    return EnumSet.of(PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION
    );
  }

  private EnumSet<PwaApplicationType> getFastTrackAppTypes() {
    return EnumSet.allOf(PwaApplicationType.class);
  }

  private EnumSet<PwaApplicationType> getPermanentDepositAppTypes() {
    return EnumSet.of(PwaApplicationType.INITIAL,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.DECOMMISSIONING);
  }


  @Test
  public void prepareAppTasks_campaignWorksNotInTaskList_whenCampaignWorksServiceAnswersNo() {

    PwaApplicationType.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getPrepareAppTasks(pwaApplicationDetail));
        assertThat(taskNamesList).doesNotContain(ApplicationTask.CAMPAIGN_WORKS.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void prepareAppTasks_campaignWorksInTaskList_forValidAppTypes_whenCampaignWorksServiceAnswersYes() {

    when(campaignWorksService.canShowInTaskList(any())).thenReturn(true);

    Set<PwaApplicationType> validApplicationTypes = getCampaignWorksAppTypes();

    validApplicationTypes.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getPrepareAppTasks(pwaApplicationDetail));
        assertThat(taskNamesList).contains(ApplicationTask.CAMPAIGN_WORKS.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void prepareAppTasks_campaignWorksNotInTaskList_forInvalidAppTypes_whenCampaignWorksServiceAnswersYes() {
    // task list service uses controller annotations and part of the integration test is making sure this markup is as expected
    when(campaignWorksService.canShowInTaskList(any())).thenReturn(true);

    Set<PwaApplicationType> invalidApplicationTypes = EnumSet.complementOf(getCampaignWorksAppTypes());

    invalidApplicationTypes.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getPrepareAppTasks(pwaApplicationDetail));
        assertThat(taskNamesList).doesNotContain(ApplicationTask.CAMPAIGN_WORKS.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }


  @Test
  public void prepareAppTasks_fastTrackNotInTaskList_whenFastTrackServiceAnswersNo() {

    PwaApplicationType.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getPrepareAppTasks(pwaApplicationDetail));
        assertThat(taskNamesList).doesNotContain(ApplicationTask.FAST_TRACK.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void prepareAppTasks_fastTrackInTaskList_forValidAppTypes_whenFastTrackServiceAnswersYes() {

    when(padFastTrackService.canShowInTaskList(any())).thenReturn(true);

    Set<PwaApplicationType> validApplicationTypes = getFastTrackAppTypes();

    validApplicationTypes.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getPrepareAppTasks(pwaApplicationDetail));
        assertThat(taskNamesList).contains(ApplicationTask.FAST_TRACK.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void prepareAppTasks_fastTrackNotInTaskList_forInvalidAppTypes_whenFastTrackServiceAnswersYes() {
    // task list service uses controller annotations and part of the integration test is making sure this markup is as expected
    when(padFastTrackService.canShowInTaskList(any())).thenReturn(true);

    Set<PwaApplicationType> invalidApplicationTypes = EnumSet.complementOf(getFastTrackAppTypes());

    invalidApplicationTypes.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getPrepareAppTasks(pwaApplicationDetail));
        assertThat(taskNamesList).doesNotContain(ApplicationTask.FAST_TRACK.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void prepareAppTasks_permDepositsNotInTaskList_whenPermDepositsServiceAnswersNo() {

    PwaApplicationType.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getPrepareAppTasks(pwaApplicationDetail));
        assertThat(taskNamesList).doesNotContain(ApplicationTask.PERMANENT_DEPOSITS.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void prepareAppTasks_permDepositsInTaskList_forValidAppTypes_whenPermDepositsServiceAnswersYes() {

    when(permanentDepositService.canShowInTaskList(any())).thenReturn(true);

    Set<PwaApplicationType> validApplicationTypes = getPermanentDepositAppTypes();

    validApplicationTypes.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getPrepareAppTasks(pwaApplicationDetail));
        assertThat(taskNamesList).contains(ApplicationTask.PERMANENT_DEPOSITS.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void prepareAppTasks_permDepositsNotInTaskList_forInvalidAppTypes_whenPermDepositsServiceAnswersYes() {
    // task list service uses controller annotations and part of the integration test is making sure this markup is as expected
    when(permanentDepositService.canShowInTaskList(any())).thenReturn(true);

    Set<PwaApplicationType> invalidApplicationTypes = EnumSet.complementOf(getPermanentDepositAppTypes());

    invalidApplicationTypes.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getPrepareAppTasks(pwaApplicationDetail));
        assertThat(taskNamesList).doesNotContain(ApplicationTask.PERMANENT_DEPOSITS.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  private List<String> getKeysFromTaskList(List<TaskListEntry> taskList) {
    return taskList.stream()
        .map(TaskListEntry::getTaskName)
        .collect(Collectors.toList());
  }
}