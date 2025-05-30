package uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

@ExtendWith(MockitoExtension.class)
class AssignmentAuditServiceTest {

  @Mock
  private AssignmentAuditRepository repository;

  @Captor
  private ArgumentCaptor<AssignmentAudit> auditArgumentCaptor;

  private AssignmentAuditService assignmentAuditService;
  private Instant fixedInstant;

  @BeforeEach
  void setUp() {
    fixedInstant = Instant.now();
    assignmentAuditService = new AssignmentAuditService(Clock.fixed(fixedInstant, ZoneId.systemDefault()), repository);
  }

  @Test
  void auditAssignment() {

    var app = new PwaApplication();
    app.setId(1);
    var assignee = new Person(1, null, null, null, null);
    var assignedBy = new Person(2, null, null, null, null);

    var assignment = new Assignment();
    assignment.setBusinessKey(app.getBusinessKey());
    assignment.setWorkflowType(app.getWorkflowType());
    assignment.setWorkflowAssignment(WorkflowAssignment.CASE_OFFICER);
    assignment.setAssigneePersonId(assignee.getId());

    assignmentAuditService.auditAssignment(assignment, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, assignedBy);

    verify(repository, times(1)).save(auditArgumentCaptor.capture());

    var audit = auditArgumentCaptor.getValue();

    assertThat(audit.getBusinessKey()).isEqualTo(assignment.getBusinessKey());
    assertThat(audit.getWorkflowType()).isEqualTo(assignment.getWorkflowType());
    assertThat(audit.getTaskKey()).isEqualTo(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW.getTaskKey());
    assertThat(audit.getAssignment()).isEqualTo(assignment.getWorkflowAssignment());
    assertThat(audit.getAssigneePersonId()).isEqualTo(assignment.getAssigneePersonId().asInt());
    assertThat(audit.getAssignedByPersonId()).isEqualTo(assignedBy.getId().asInt());
    assertThat(audit.getAssignmentTimestamp()).isEqualTo(fixedInstant);

  }

}
