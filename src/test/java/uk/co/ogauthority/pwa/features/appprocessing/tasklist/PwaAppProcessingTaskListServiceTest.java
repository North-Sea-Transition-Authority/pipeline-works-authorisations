package uk.co.ogauthority.pwa.features.appprocessing.tasklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsProperties;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles(profiles = { "integration-test", "test" })
@EnableConfigurationProperties(value = AnalyticsProperties.class)
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
public class PwaAppProcessingTaskListServiceTest {

  @Autowired
  private PwaAppProcessingTaskService processingTaskService;

  @Autowired
  private PwaAppProcessingTaskListService taskListService;

  @Autowired
  private EntityManager entityManager;

  private PwaApplicationDetail pwaApplicationDetail;

  private PwaAppProcessingContext processingContext;

  @Before
  public void setUp() throws IllegalAccessException {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var request = new ConsultationRequest();
    entityManager.persist(request);

    var involvement = PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", request, true);

    processingContext = new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        EnumSet.complementOf(EnumSet.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY, PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY)),
        null,
        involvement,
        Set.of());

    taskListService = new PwaAppProcessingTaskListService(processingTaskService);

  }

  @Test
  @Transactional
  public void getTaskListGroups_caseManagementIndustry_andPwaManager() {

    var taskListGroups = taskListService.getTaskListGroups(processingContext);

    assertThat(taskListGroups)
        .extracting(TaskListGroup::getGroupName, TaskListGroup::getDisplayOrder)
        .containsExactly(
            tuple(TaskRequirement.REQUIRED.getDisplayName(), TaskRequirement.REQUIRED.getDisplayOrder()),
            tuple(TaskRequirement.OPTIONAL.getDisplayName(), TaskRequirement.OPTIONAL.getDisplayOrder())
        );

    assertThat(taskListGroups.get(0).getTaskListEntries())
        .extracting(TaskListEntry::getTaskName, TaskListEntry::getRoute)
        .containsExactly(
            tuple(PwaAppProcessingTask.INITIAL_REVIEW.getTaskName(), PwaAppProcessingTask.INITIAL_REVIEW.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getTaskName(), PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.CONSULTATIONS.getTaskName(), PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext)),
            // APPROVE_OPTIONS route has content based on independently tested specific conditions
            tuple(PwaAppProcessingTask.APPROVE_OPTIONS.getTaskName(), PwaAppProcessingTask.APPROVE_OPTIONS.getRoute(processingContext)),
            // CLOSE_OUT_OPTIONS route has content based on independently tested specific conditions
            tuple(PwaAppProcessingTask.CLOSE_OUT_OPTIONS.getTaskName(), PwaAppProcessingTask.CLOSE_OUT_OPTIONS.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.PUBLIC_NOTICE.getTaskName(), PwaAppProcessingTask.PUBLIC_NOTICE.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName(), PwaAppProcessingTask.PREPARE_CONSENT.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.ALLOCATE_RESPONDER.getTaskName(), PwaAppProcessingTask.ALLOCATE_RESPONDER.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.CONSULTATION_RESPONSE.getTaskName(), PwaAppProcessingTask.CONSULTATION_RESPONSE.getRoute(processingContext))
        );

    assertThat(taskListGroups.get(1).getTaskListEntries())
        .extracting(TaskListEntry::getTaskName, TaskListEntry::getRoute)
        .containsExactly(
            // CHANGE_OPTIONS_APPROVAL_DEADLINE route has content based on independently tested specific conditions
            tuple(PwaAppProcessingTask.CHANGE_OPTIONS_APPROVAL_DEADLINE.getTaskName(), PwaAppProcessingTask.CHANGE_OPTIONS_APPROVAL_DEADLINE.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.CONSULTEE_ADVICE.getTaskName(), PwaAppProcessingTask.CONSULTEE_ADVICE.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.RFI.getTaskName(), PwaAppProcessingTask.RFI.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.ADD_NOTE_OR_DOCUMENT.getTaskName(), PwaAppProcessingTask.ADD_NOTE_OR_DOCUMENT.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.WITHDRAW_APPLICATION.getTaskName(), PwaAppProcessingTask.WITHDRAW_APPLICATION.getRoute(processingContext))
        );

    taskListGroups.stream()
        .flatMap(group -> group.getTaskListEntries().stream())
        .forEach(entry -> {

          var resolvedTask = PwaAppProcessingTask.resolveFromTaskName(entry.getTaskName());
          var lockedTasks = List.of(
              PwaAppProcessingTask.APPROVE_OPTIONS,
              PwaAppProcessingTask.CLOSE_OUT_OPTIONS,
              PwaAppProcessingTask.CHANGE_OPTIONS_APPROVAL_DEADLINE,
              PwaAppProcessingTask.PREPARE_CONSENT,
              PwaAppProcessingTask.CONSULTATIONS,
              PwaAppProcessingTask.PUBLIC_NOTICE,
              PwaAppProcessingTask.INITIAL_REVIEW,
              PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION
          );

          if (lockedTasks.contains(resolvedTask)) {
            assertThat(entry.getTaskState()).isEqualTo(TaskState.LOCK);
          } else {
            assertThat(entry.getTaskState()).isEqualTo(TaskState.EDIT);
          }

        });

  }

  @Test
  @Transactional
  public void getTaskListGroups_noOptional() {

    var request = new ConsultationRequest();
    entityManager.persist(request);

    processingContext = new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        EnumSet.of(
            PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY,
            PwaAppProcessingPermission.ASSIGN_RESPONDER,
            PwaAppProcessingPermission.CONSULTATION_RESPONDER),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", request, true),
        Set.of());

    var taskListGroups = taskListService.getTaskListGroups(processingContext);

    assertThat(taskListGroups)
        .extracting(TaskListGroup::getGroupName, TaskListGroup::getDisplayOrder)
        .containsExactly(
            tuple(TaskRequirement.REQUIRED.getDisplayName(), TaskRequirement.REQUIRED.getDisplayOrder())
        );

    assertThat(taskListGroups.get(0).getTaskListEntries())
        .extracting(TaskListEntry::getTaskName, TaskListEntry::getRoute)
        .containsExactly(
            tuple(PwaAppProcessingTask.INITIAL_REVIEW.getTaskName(), PwaAppProcessingTask.INITIAL_REVIEW.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getTaskName(), PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.CONSULTATIONS.getTaskName(), PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.PUBLIC_NOTICE.getTaskName(), PwaAppProcessingTask.PUBLIC_NOTICE.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.PREPARE_CONSENT.getTaskName(), PwaAppProcessingTask.PREPARE_CONSENT.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.ALLOCATE_RESPONDER.getTaskName(), PwaAppProcessingTask.ALLOCATE_RESPONDER.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.CONSULTATION_RESPONSE.getTaskName(), PwaAppProcessingTask.CONSULTATION_RESPONSE.getRoute(processingContext))
        );

  }

  @Test
  @Transactional
  public void getTaskListGroups_entriesLockedWhenOpenConsentReview_andIsCaseOfficer() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        pwaApplicationDetail.getPwaApplication(),
        Set.of(
            ApplicationInvolvementDtoTestUtil.InvolvementFlag.CASE_OFFICER_STAGE_AND_USER_ASSIGNED,
            ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION,
            ApplicationInvolvementDtoTestUtil.InvolvementFlag.OPEN_CONSENT_REVIEW)
    );

    processingContext = new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        EnumSet.of(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA),
        null,
        appInvolvement,
        Set.of());

    var taskListGroups = taskListService.getTaskListGroups(processingContext);

    taskListGroups.stream()
        .flatMap(group -> group.getTaskListEntries().stream())
        .forEach(entry -> {

          if (entry.getTaskName().equals(PwaAppProcessingTask.ADD_NOTE_OR_DOCUMENT.getTaskName())) {
            assertThat(entry.getTaskState()).isEqualTo(TaskState.EDIT);
          } else {
            assertThat(entry.getTaskState()).isEqualTo(TaskState.LOCK);
          }

        });

  }

  @Test
  @Transactional
  public void getTaskListGroups_entriesLockedWhenIndustry_appContact() {

    processingContext = new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        EnumSet.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY),
        null,
        ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
            pwaApplicationDetail.getPwaApplication(),
            Set.of(PwaContactRole.PREPARER)),
        Set.of());

    var taskListGroups = taskListService.getTaskListGroups(processingContext);

    taskListGroups.stream()
        .flatMap(group -> group.getTaskListEntries().stream())
        .forEach(entry -> assertThat(entry.getTaskState()).isEqualTo(TaskState.LOCK));

  }

}
