package uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;

@Repository
public interface AssignmentRepository extends CrudRepository<Assignment, Integer> {

  Optional<Assignment> findByBusinessKeyAndWorkflowTypeAndWorkflowAssignment(Integer businessKey,
                                                                             WorkflowType workflowType,
                                                                             WorkflowAssignment workflowAssignment);

  List<Assignment> findByAssigneePersonId(PersonId personId);

  List<Assignment> findByBusinessKeyAndWorkflowType(Integer businessKey, WorkflowType workflowType);

  List<Assignment> findByBusinessKeyAndWorkflowTypeAndAssigneePersonId(Integer businessKey,
                                                                       WorkflowType workflowType,
                                                                       PersonId personId);

  Optional<Assignment> findByBusinessKeyAndWorkflowAssignmentAndWorkflowType(Integer businessKey, WorkflowAssignment workflowAssignment,
                                                                         WorkflowType workflowType);

}
