package uk.co.ogauthority.pwa.service.appprocessing.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.EnumSet;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskRequirement;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
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
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var request = new ConsultationRequest();
    entityManager.persist(request);

    processingContext = new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        EnumSet.allOf(PwaAppProcessingPermission.class),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", request)
    );

    taskListService = new PwaAppProcessingTaskListService(processingTaskService);

  }

  @Test
  @Transactional
  public void getTaskListGroups() {

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
            tuple(PwaAppProcessingTask.ACCEPT_APPLICATION.getTaskName(), PwaAppProcessingTask.ACCEPT_APPLICATION.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.CASE_SETUP.getTaskName(), PwaAppProcessingTask.CASE_SETUP.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.CONSULTATIONS.getTaskName(), PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext)),
            // APPROVE_OPTIONS route has content based on independently tested specific conditions
            tuple(PwaAppProcessingTask.APPROVE_OPTIONS.getTaskName(), null),
            tuple(PwaAppProcessingTask.PUBLIC_NOTICE.getTaskName(), PwaAppProcessingTask.PUBLIC_NOTICE.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.DECISION.getTaskName(), PwaAppProcessingTask.DECISION.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.ALLOCATE_RESPONDER.getTaskName(), PwaAppProcessingTask.ALLOCATE_RESPONDER.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.CONSULTATION_RESPONSE.getTaskName(), PwaAppProcessingTask.CONSULTATION_RESPONSE.getRoute(processingContext))
        );

    assertThat(taskListGroups.get(1).getTaskListEntries())
        .extracting(TaskListEntry::getTaskName, TaskListEntry::getRoute)
        .containsExactly(
            tuple(PwaAppProcessingTask.CONSULTEE_ADVICE.getTaskName(), PwaAppProcessingTask.CONSULTEE_ADVICE.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.ALLOCATE_CASE_OFFICER.getTaskName(), PwaAppProcessingTask.ALLOCATE_CASE_OFFICER.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.RFI.getTaskName(), PwaAppProcessingTask.RFI.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.ADD_NOTE_OR_DOCUMENT.getTaskName(), PwaAppProcessingTask.ADD_NOTE_OR_DOCUMENT.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.WITHDRAW_APPLICATION.getTaskName(), PwaAppProcessingTask.WITHDRAW_APPLICATION.getRoute(processingContext))
        );

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
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", request)
    );

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
            tuple(PwaAppProcessingTask.ACCEPT_APPLICATION.getTaskName(), PwaAppProcessingTask.ACCEPT_APPLICATION.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.CONSULTATIONS.getTaskName(), PwaAppProcessingTask.CONSULTATIONS.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.PUBLIC_NOTICE.getTaskName(), PwaAppProcessingTask.PUBLIC_NOTICE.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.DECISION.getTaskName(), PwaAppProcessingTask.DECISION.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.ALLOCATE_RESPONDER.getTaskName(), PwaAppProcessingTask.ALLOCATE_RESPONDER.getRoute(processingContext)),
            tuple(PwaAppProcessingTask.CONSULTATION_RESPONSE.getTaskName(), PwaAppProcessingTask.CONSULTATION_RESPONSE.getRoute(processingContext))
        );


  }

}
