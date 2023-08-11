package uk.co.ogauthority.pwa.features.reassignment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class CaseReassignmentService {

  private final CaseReassignmentRepository reassignmentRepository;

  private final CaseReassignmentOfficerValidator caseReassignmentOfficerValidator;

  private final CaseReassignmentCasesValidator caseReassignmentCasesValidator;

  public CaseReassignmentService(CaseReassignmentRepository reassignmentRepository,
                                 CaseReassignmentOfficerValidator caseReassignmentOfficerValidator,
                                 CaseReassignmentCasesValidator caseReassignmentCasesValidator) {
    this.reassignmentRepository = reassignmentRepository;
    this.caseReassignmentOfficerValidator = caseReassignmentOfficerValidator;
    this.caseReassignmentCasesValidator = caseReassignmentCasesValidator;
  }

  public List<CaseReassignmentView> findAllReassignableCases() {
    return StreamSupport.stream(reassignmentRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  public List<CaseReassignmentView> findAllCasesByApplicationId(List<Integer> applicationIds) {
    return reassignmentRepository.findAllByApplicationIdIn(applicationIds);
  }

  public BindingResult validateOfficerForm(CaseReassignmentOfficerForm form, BindingResult bindingResult) {
    caseReassignmentOfficerValidator.validate(form, bindingResult);
    return bindingResult;
  }

  public BindingResult validateCasesForm(CaseReassignmentCasesForm form, BindingResult bindingResult) {
    caseReassignmentCasesValidator.validate(form, bindingResult);
    return bindingResult;
  }
}
