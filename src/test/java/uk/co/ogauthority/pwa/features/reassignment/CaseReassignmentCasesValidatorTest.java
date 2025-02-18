package uk.co.ogauthority.pwa.features.reassignment;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class CaseReassignmentCasesValidatorTest {

  private CaseReassignmentCasesValidator caseReassignmentCasesValidator;

  @BeforeEach
  void setup() {
    caseReassignmentCasesValidator = new CaseReassignmentCasesValidator();
  }

  @Test
  void supports_CaseReassignmentForm() {
    assertTrue(caseReassignmentCasesValidator.supports(CaseReassignmentCasesForm.class));
    assertFalse(caseReassignmentCasesValidator.supports(CaseReassignmentFilterForm.class));
  }

  @Test
  void validator_allEmpty() {
    var caseReassignmentCasesForm = new CaseReassignmentCasesForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(caseReassignmentCasesValidator, caseReassignmentCasesForm);
    assertThat(errorsMap).isNotEmpty();
    assertThat(errorsMap).contains(entry("selectedApplicationIds", Set.of("selectedApplicationIds" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validator_allValid() {
    var caseReassignmentCasesForm = new CaseReassignmentCasesForm();
    caseReassignmentCasesForm.setSelectedApplicationIds(List.of("71", "52"));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(caseReassignmentCasesValidator, caseReassignmentCasesForm);
    assertThat(errorsMap).isEmpty();
  }
}
