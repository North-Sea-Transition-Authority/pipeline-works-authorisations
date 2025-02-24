package uk.co.ogauthority.pwa.features.reassignment;

import static org.mockito.Mockito.verify;

import jakarta.persistence.criteria.Predicate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;

@ExtendWith(MockitoExtension.class)
class CaseReassignmentServiceTest {
  @Mock
  CaseReassignmentRepository reassignmentRepository;

  @Mock
  CaseReassignmentOfficerValidator caseReassignmentOfficerValidator;

  @Mock
  CaseReassignmentCasesValidator caseReassignmentCasesValidator;

  @Captor
  ArgumentCaptor<Predicate[]> criteriaCaptor;

  CaseReassignmentService service;

  @BeforeEach
  void setup() {
    service = new CaseReassignmentService(reassignmentRepository, caseReassignmentOfficerValidator,
        caseReassignmentCasesValidator);
  }

  @Test
  void getReassignableCases_CaseOfficerFilter() {
    service.findAllReassignableCases();
    verify(reassignmentRepository).findAll();
  }

  @Test
  void validateOfficerForm() {
    var form = new CaseReassignmentOfficerForm();
    form.setAssignedCaseOfficerPersonId(1);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validateOfficerForm(form, bindingResult);

    verify(caseReassignmentOfficerValidator).validate(form, bindingResult);
  }

  @Test
  void validateCasesForm() {
    var form = new CaseReassignmentCasesForm();
    form.setSelectedApplicationIds(List.of("test"));
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validateCasesForm(form, bindingResult);

    verify(caseReassignmentCasesValidator).validate(form, bindingResult);
  }
}
