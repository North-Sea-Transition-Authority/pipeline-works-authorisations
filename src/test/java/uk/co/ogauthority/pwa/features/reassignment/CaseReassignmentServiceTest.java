package uk.co.ogauthority.pwa.features.reassignment;

import static org.mockito.Mockito.verify;

import java.util.List;
import javax.persistence.criteria.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;

@RunWith(MockitoJUnitRunner.class)
public class CaseReassignmentServiceTest {
  @Mock
  CaseReassignmentRepository reassignmentRepository;

  @Mock
  CaseReassignmentOfficerValidator caseReassignmentOfficerValidator;

  @Mock
  CaseReassignmentCasesValidator caseReassignmentCasesValidator;

  @Captor
  ArgumentCaptor<Predicate[]> criteriaCaptor;

  CaseReassignmentService service;

  @Before
  public void setup() {
    service = new CaseReassignmentService(reassignmentRepository, caseReassignmentOfficerValidator,
        caseReassignmentCasesValidator);
  }

  @Test
  public void getReassignableCases_CaseOfficerFilter() {
    service.findAllReassignableCases();
    verify(reassignmentRepository).findAll();
  }

  @Test
  public void validateOfficerForm() {
    var form = new CaseReassignmentOfficerForm();
    form.setAssignedCaseOfficerPersonId(1);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validateOfficerForm(form, bindingResult);

    verify(caseReassignmentOfficerValidator).validate(form, bindingResult);
  }

  @Test
  public void validateCasesForm() {
    var form = new CaseReassignmentCasesForm();
    form.setSelectedApplicationIds(List.of("test"));
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validateCasesForm(form, bindingResult);

    verify(caseReassignmentCasesValidator).validate(form, bindingResult);
  }
}
