package uk.co.ogauthority.pwa.features.reassignment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;

@Service
public class CaseReassignmentService {

  private final CaseReassignmentRepository reassignmentRepository;

  public CaseReassignmentService(CaseReassignmentRepository reassignmentRepository) {
    this.reassignmentRepository = reassignmentRepository;
  }

  public List<CaseReassignmentView> findAllReassignableCases() {
    return StreamSupport.stream(reassignmentRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  public List<CaseReassignmentView> findAllCasesByPadId(List<Integer> padIds) {
    return reassignmentRepository.findAllByPadIdIn(padIds);
  }
}
