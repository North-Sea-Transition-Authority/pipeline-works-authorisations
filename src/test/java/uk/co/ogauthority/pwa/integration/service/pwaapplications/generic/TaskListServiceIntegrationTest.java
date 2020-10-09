package uk.co.ogauthority.pwa.integration.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTaskGroup;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaViewService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationTaskService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListEntryFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.options.OptionsTemplateService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.supplementarydocs.SupplementaryDocumentsService;
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
public class TaskListServiceIntegrationTest {

  private TaskListService taskListService;

  @Autowired
  private PwaApplicationCreationService pwaApplicationCreationService;

  @Autowired
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  private ApplicationBreadcrumbService breadcrumbService;

  @Autowired
  private TaskListEntryFactory taskListEntryFactory;

  @Autowired
  private ApplicationTaskService applicationTaskService;

  // this needs to be mocked so we dont try increment a sequence that doesnt exist in h2
  @MockBean
  private PwaApplicationReferencingService pwaApplicationReferencingService;

  @MockBean
  private CampaignWorksService campaignWorksService;

  @MockBean
  private PadFastTrackService padFastTrackService;

  @MockBean
  private PermanentDepositService permanentDepositService;

  @MockBean
  private DepositDrawingsService depositDrawingsService;

  @MockBean
  private MasterPwaViewService masterPwaViewService;

  @MockBean
  private OptionsTemplateService optionsTemplateService;

  @MockBean
  private SupplementaryDocumentsService supplementaryDocumentsService;

  @MockBean
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setup() {

    // by default, conditional app tasks not shown
    pwaApplicationDetail = pwaApplicationCreationService.createInitialPwaApplication(new WebUserAccount(1));
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    taskListService = new TaskListService(
        breadcrumbService,
        taskListEntryFactory,
        applicationTaskService,
        masterPwaViewService,
        applicationUpdateRequestViewService
    );

    when(optionsTemplateService.canShowInTaskList(any())).thenReturn(true);
    when(supplementaryDocumentsService.canShowInTaskList(any())).thenReturn(true);

  }

  @Test
  public void getApplicationTasks_forEveryAppType_assertAllTasksWithNoCrossSectionDependencies() {

    PwaApplicationType.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getApplicationTaskListEntries(pwaApplicationDetail));

        switch (appType) {
          case INITIAL:
          case CAT_1_VARIATION:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.FIELD_INFORMATION.getDisplayName(),
                ApplicationTask.APPLICATION_USERS.getDisplayName(),
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
                ApplicationTask.PIPELINE_OTHER_PROPERTIES.getDisplayName(),
                ApplicationTask.DESIGN_OP_CONDITIONS.getDisplayName(),
                ApplicationTask.PARTNER_LETTERS.getDisplayName()
            );
            break;
          case DEPOSIT_CONSENT:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.FIELD_INFORMATION.getDisplayName(),
                ApplicationTask.APPLICATION_USERS.getDisplayName(),
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING.getDisplayName(),
                ApplicationTask.CROSSING_AGREEMENTS.getDisplayName(),
                ApplicationTask.LOCATION_DETAILS.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName()
            );
            break;
          case DECOMMISSIONING:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.FIELD_INFORMATION.getDisplayName(),
                ApplicationTask.APPLICATION_USERS.getDisplayName(),
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING.getDisplayName(),
                ApplicationTask.LOCATION_DETAILS.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName(),
                ApplicationTask.PARTNER_LETTERS.getDisplayName(),
                ApplicationTask.CROSSING_AGREEMENTS.getDisplayName(),
                ApplicationTask.TECHNICAL_DRAWINGS.getDisplayName(),
                ApplicationTask.PIPELINES.getDisplayName(),
                ApplicationTask.PIPELINES_HUOO.getDisplayName()
            );
            break;
          case OPTIONS_VARIATION:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.FIELD_INFORMATION.getDisplayName(),
                ApplicationTask.APPLICATION_USERS.getDisplayName(),
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.OPTIONS_TEMPLATE.getDisplayName(),
                ApplicationTask.SUPPLEMENTARY_DOCUMENTS.getDisplayName()
            );
            break;
          case CAT_2_VARIATION:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.FIELD_INFORMATION.getDisplayName(),
                ApplicationTask.APPLICATION_USERS.getDisplayName(),
                ApplicationTask.PROJECT_INFORMATION.getDisplayName(),
                ApplicationTask.CROSSING_AGREEMENTS.getDisplayName(),
                ApplicationTask.LOCATION_DETAILS.getDisplayName(),
                ApplicationTask.HUOO.getDisplayName(),
                ApplicationTask.PIPELINES.getDisplayName(),
                ApplicationTask.PIPELINES_HUOO.getDisplayName(),
                ApplicationTask.TECHNICAL_DRAWINGS.getDisplayName(),
                ApplicationTask.PARTNER_LETTERS.getDisplayName()
            );
            break;
          case HUOO_VARIATION:
            assertThat(taskNamesList).containsOnly(
                ApplicationTask.FIELD_INFORMATION.getDisplayName(),
                ApplicationTask.APPLICATION_USERS.getDisplayName(),
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
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING
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
        PwaApplicationType.DECOMMISSIONING);
  }


  @Test
  public void getApplicationTasks_campaignWorksNotInTaskList_whenCampaignWorksServiceAnswersNo() {

    PwaApplicationType.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getApplicationTaskListEntries(pwaApplicationDetail));
        assertThat(taskNamesList).doesNotContain(ApplicationTask.CAMPAIGN_WORKS.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void getApplicationTasks_campaignWorksInTaskList_forValidAppTypes_whenCampaignWorksServiceAnswersYes() {

    when(campaignWorksService.canShowInTaskList(any())).thenReturn(true);

    Set<PwaApplicationType> validApplicationTypes = getCampaignWorksAppTypes();

    validApplicationTypes.forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getApplicationTaskListEntries(pwaApplicationDetail));
        assertThat(taskNamesList).contains(ApplicationTask.CAMPAIGN_WORKS.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void getApplicationTasks_campaignWorksNotInTaskList_forInvalidAppTypes_whenCampaignWorksServiceAnswersYes() {
    // task list service uses controller annotations and part of the integration test is making sure this markup is as expected
    when(campaignWorksService.canShowInTaskList(any())).thenReturn(true);

    Set<PwaApplicationType> invalidApplicationTypes = EnumSet.complementOf(getCampaignWorksAppTypes());

    invalidApplicationTypes.forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getApplicationTaskListEntries(pwaApplicationDetail));
        assertThat(taskNamesList).doesNotContain(ApplicationTask.CAMPAIGN_WORKS.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }


  @Test
  public void getApplicationTasks_fastTrackNotInTaskList_whenFastTrackServiceAnswersNo() {

    PwaApplicationType.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getApplicationTaskListEntries(pwaApplicationDetail));
        assertThat(taskNamesList).doesNotContain(ApplicationTask.FAST_TRACK.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void getApplicationTasks_fastTrackInTaskList_forValidAppTypes_whenFastTrackServiceAnswersYes() {

    when(padFastTrackService.canShowInTaskList(any())).thenReturn(true);

    Set<PwaApplicationType> validApplicationTypes = getFastTrackAppTypes();

    validApplicationTypes.forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getApplicationTaskListEntries(pwaApplicationDetail));
        assertThat(taskNamesList).contains(ApplicationTask.FAST_TRACK.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void getApplicationTasks_fastTrackNotInTaskList_forInvalidAppTypes_whenFastTrackServiceAnswersYes() {
    // task list service uses controller annotations and part of the integration test is making sure this markup is as expected
    when(padFastTrackService.canShowInTaskList(any())).thenReturn(true);

    Set<PwaApplicationType> invalidApplicationTypes = EnumSet.complementOf(getFastTrackAppTypes());

    invalidApplicationTypes.forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getApplicationTaskListEntries(pwaApplicationDetail));
        assertThat(taskNamesList).doesNotContain(ApplicationTask.FAST_TRACK.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void getApplicationTasks_permDepositsNotInTaskList_whenPermDepositsServiceAnswersNo() {

    PwaApplicationType.stream().forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getApplicationTaskListEntries(pwaApplicationDetail));
        assertThat(taskNamesList).doesNotContain(ApplicationTask.PERMANENT_DEPOSITS.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void getApplicationTasks_permDepositsInTaskList_forValidAppTypes_whenPermDepositsServiceAnswersYes() {

    when(permanentDepositService.canShowInTaskList(any())).thenReturn(true);

    Set<PwaApplicationType> validApplicationTypes = getPermanentDepositAppTypes();

    validApplicationTypes.forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getApplicationTaskListEntries(pwaApplicationDetail));
        assertThat(taskNamesList).contains(ApplicationTask.PERMANENT_DEPOSITS.getDisplayName());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type: " + appType + "\n" + e.getMessage(), e);
      }

    });

  }

  @Test
  public void getApplicationTasks_permDepositsNotInTaskList_forInvalidAppTypes_whenPermDepositsServiceAnswersYes() {
    // task list service uses controller annotations and part of the integration test is making sure this markup is as expected
    when(permanentDepositService.canShowInTaskList(any())).thenReturn(true);

    Set<PwaApplicationType> invalidApplicationTypes = EnumSet.complementOf(getPermanentDepositAppTypes());

    invalidApplicationTypes.forEach(appType -> {
      try {
        pwaApplication.setApplicationType(appType);
        var taskNamesList = getKeysFromTaskList(taskListService.getApplicationTaskListEntries(pwaApplicationDetail));
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

  private void setupConditionalTaskServices(boolean tasksShow) {

    when(permanentDepositService.canShowInTaskList(any())).thenReturn(tasksShow);
    when(campaignWorksService.canShowInTaskList(any())).thenReturn(tasksShow);
    when(padFastTrackService.canShowInTaskList(any())).thenReturn(tasksShow);
    when(depositDrawingsService.canShowInTaskList(any())).thenReturn(tasksShow);

  }

  @Test
  public void getTaskListGroups_whenAllTasksShown_confirmOrderingOfGroupsAndTasksMatchesEnumDefinition() {
    setupConditionalTaskServices(true);

    PwaApplicationType.stream().forEach(applicationType -> {

      pwaApplicationDetail.getPwaApplication().setApplicationType(applicationType);

      var taskListGroups = taskListService.getTaskListGroups(pwaApplicationDetail);

      // loop over all task groups and make sure the constructed groups are in the same order as group enum definition
      for (int i = 0; i < taskListGroups.size(); i++) {

        // get the actual group we are processing from the list and its display order
        var actualTaskListGroup = taskListGroups.get(i);
        var actualGroupDisplayOrder = actualTaskListGroup.getDisplayOrder();

        // for the actual group's siblings in the list (next and previous, where applicable), get the intended display order of each
        var previousGroup = i == 0 ? null : taskListGroups.get(i - 1);
        var nextGroup = i == taskListGroups.size() - 1 ? null : taskListGroups.get(i + 1);

        var previousGroupDisplayOrder = Optional.ofNullable(previousGroup)
            .map(group -> ApplicationTaskGroup.resolveFromName(previousGroup.getGroupName()))
            .map(ApplicationTaskGroup::getDisplayOrder)
            .orElse(-1);

        var nextGroupDisplayOrder = Optional.ofNullable(nextGroup)
            .map(group -> ApplicationTaskGroup.resolveFromName(group.getGroupName()))
            .map(ApplicationTaskGroup::getDisplayOrder)
            .orElse(999);

        try {

          // ensure that the ordering is as intended, previous group should be before our actual group, and the next group should be after our actual group
          boolean previousGroupOrderIsCorrect = previousGroupDisplayOrder < actualGroupDisplayOrder;
          boolean nextGroupOrderIsCorrect = nextGroupDisplayOrder > actualGroupDisplayOrder;
          assertThat(previousGroupOrderIsCorrect && nextGroupOrderIsCorrect);

        } catch (AssertionError e) {
          throw new AssertionError(String.format("Group out of order! Group name [%s] at index [%s] for app type [%s] \n",
              actualTaskListGroup.getGroupName(),
              i,
              e));
        }

        var taskListEntries = actualTaskListGroup.getTaskListEntries();

        for (int j = 0; j < taskListEntries.size(); j++) {

          var actualTaskListEntry = taskListEntries.get(j);
          var actualEntryDisplayOrder = actualTaskListEntry.getDisplayOrder();

          // for the actual entry's siblings in the list (next and previous, where applicable), get the intended display order of each
          var previousEntry = j == 0 ? null : taskListEntries.get(j - 1);
          var nextEntry = j == taskListEntries.size() - 1 ? null : taskListEntries.get(j + 1);

          var previousEntryDisplayOrder = Optional.ofNullable(previousEntry)
              .map(entry -> ApplicationTask.resolveFromName(entry.getTaskName()))
              .map(ApplicationTask::getDisplayOrder)
              .orElse(-1);

          var nextEntryDisplayOrder = Optional.ofNullable(nextEntry)
              .map(entry -> ApplicationTask.resolveFromName(entry.getTaskName()))
              .map(ApplicationTask::getDisplayOrder)
              .orElse(999);

          try {

            // ensure that the ordering is as intended, previous entry should be before our actual entry, and the next entry should be after our actual entry
            boolean previousEntryOrderIsCorrect = previousEntryDisplayOrder < actualEntryDisplayOrder;
            boolean nextEntryOrderIsCorrect = nextEntryDisplayOrder > actualEntryDisplayOrder;
            assertThat(previousEntryOrderIsCorrect && nextEntryOrderIsCorrect);

          } catch (AssertionError e) {
            throw new AssertionError(String.format("Group Task out of order! Group: %s Task position: %s for app type %s \n %s",
                actualTaskListGroup.getGroupName(),
                j,
                applicationType.name(),
                e.getMessage()),
                e
            );
          }
        }

      }


    });

  }
}