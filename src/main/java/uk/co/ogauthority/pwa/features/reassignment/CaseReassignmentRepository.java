package uk.co.ogauthority.pwa.features.reassignment;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface CaseReassignmentRepository extends CrudRepository<CaseReassignmentView, Integer> {

  List<CaseReassignmentView> findAllByAssignedCaseOfficerPersonId(Integer assignedCaseOfficerPersonId);

  List<CaseReassignmentView> findAllByApplicationIdIn(List<Integer> applicationId);

}
