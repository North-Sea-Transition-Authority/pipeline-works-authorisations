package uk.co.ogauthority.pwa.features.reassignment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class CaseReassignmentService {

  private final CaseReassignmentRepository reassignmentRepository;

  private final CaseReassignmentValidator caseReassignmentValidator;

  public CaseReassignmentService(CaseReassignmentRepository reassignmentRepository,
                                 CaseReassignmentValidator caseReassignmentValidator) {
    this.reassignmentRepository = reassignmentRepository;
    this.caseReassignmentValidator = caseReassignmentValidator;
  }

  public List<CaseReassignmentView> findAllReassignableCases() {
    return StreamSupport.stream(reassignmentRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  public List<CaseReassignmentView> findAllCasesByApplicationId(List<Integer> applicationIds) {
    return reassignmentRepository.findAllByApplicationIdIn(applicationIds);
  }

  public BindingResult validateForm(CaseReassignmentSelectorForm form, BindingResult bindingResult) {
    caseReassignmentValidator.validate(form, bindingResult);
    return bindingResult;
  }
}
