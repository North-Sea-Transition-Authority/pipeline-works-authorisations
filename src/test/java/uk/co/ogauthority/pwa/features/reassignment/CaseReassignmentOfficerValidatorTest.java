package uk.co.ogauthority.pwa.features.reassignment;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class CaseReassignmentOfficerValidatorTest {

  private CaseReassignmentOfficerValidator caseReassignmentOfficerValidator;

  @BeforeEach
  void setup() {
    caseReassignmentOfficerValidator = new CaseReassignmentOfficerValidator();
  }

  @Test
  void supports_CaseReassignmentForm() {
    assertTrue(caseReassignmentOfficerValidator.supports(CaseReassignmentOfficerForm.class));
    assertFalse(caseReassignmentOfficerValidator.supports(CaseReassignmentFilterForm.class));
  }

  @Test
  void validator_allEmpty() {
    var caseReassignmentOfficerForm = new CaseReassignmentOfficerForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(caseReassignmentOfficerValidator, caseReassignmentOfficerForm);
    assertThat(errorsMap).isNotEmpty();
    assertThat(errorsMap).contains(entry("assignedCaseOfficerPersonId", Set.of("assignedCaseOfficerPersonId" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validator_allValid() {
    var caseReassignmentOfficerForm = new CaseReassignmentOfficerForm();
    caseReassignmentOfficerForm.setAssignedCaseOfficerPersonId(1000);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(caseReassignmentOfficerValidator, caseReassignmentOfficerForm);
    assertThat(errorsMap).isEmpty();
  }
}
