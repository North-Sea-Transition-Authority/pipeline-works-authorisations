package uk.co.ogauthority.pwa.repository.workflow.assignment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;

@Repository
public interface AssignmentRepository extends CrudRepository<Assignment, Integer> {

  Optional<Assignment> findByBusinessKeyAndWorkflowTypeAndWorkflowAssignment(Integer businessKey,
                                                                             WorkflowType workflowType,
                                                                             WorkflowAssignment workflowAssignment);

  List<Assignment> findByAssigneePersonId(PersonId personId);

  List<Assignment> findByBusinessKeyAndWorkflowType(Integer businessKey, WorkflowType workflowType);

}
