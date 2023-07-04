package uk.co.ogauthority.pwa.features.reassignment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;

@Service
public class ReviewIdentifierService {

  private final CaseReassignmentRepository reassignmentRepository;

  public ReviewIdentifierService(CaseReassignmentRepository reassignmentRepository) {
    this.reassignmentRepository = reassignmentRepository;
  }

  public List<CaseReassignmentView> findAllReassignableCases(Integer caseOfficerId) {
    if (caseOfficerId != null) {
      return reassignmentRepository.findAllByAssignedCaseOfficerPersonId(caseOfficerId);
    }
    return StreamSupport.stream(reassignmentRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }
}
