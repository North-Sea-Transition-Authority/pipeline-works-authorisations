package uk.co.ogauthority.pwa.repository.pwaapplications.search;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaAppAssignmentView;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;

@Repository
public interface PwaAppAssignmentViewRepository extends CrudRepository<PwaAppAssignmentView, Integer> {

  List<PwaAppAssignmentView> findAllByAssignmentAndPwaApplicationIdIn(WorkflowAssignment assignment,
                                                                      List<Integer> pwaApplicationIds);

}