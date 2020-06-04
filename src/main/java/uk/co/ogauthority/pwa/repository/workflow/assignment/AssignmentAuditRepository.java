package uk.co.ogauthority.pwa.repository.workflow.assignment;

import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.AssignmentAudit;

public interface AssignmentAuditRepository extends CrudRepository<AssignmentAudit, Integer> {
}
