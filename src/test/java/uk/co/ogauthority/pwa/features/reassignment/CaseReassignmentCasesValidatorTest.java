package uk.co.ogauthority.pwa.features.reassignment;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class CaseReassignmentCasesValidatorTest {

  private CaseReassignmentCasesValidator caseReassignmentCasesValidator;

  @Before
  public void setup() {
    caseReassignmentCasesValidator = new CaseReassignmentCasesValidator();
  }

  @Test
  public void supports_CaseReassignmentForm() {
    assertTrue(caseReassignmentCasesValidator.supports(CaseReassignmentCasesForm.class));
    assertFalse(caseReassignmentCasesValidator.supports(CaseReassignmentFilterForm.class));
  }

  @Test
  public void validator_allEmpty() {
    var caseReassignmentCasesForm = new CaseReassignmentCasesForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(caseReassignmentCasesValidator, caseReassignmentCasesForm);
    assertThat(errorsMap).isNotEmpty();
    assertThat(errorsMap).contains(entry("selectedApplicationIds", Set.of("selectedApplicationIds" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validator_allValid() {
    var caseReassignmentCasesForm = new CaseReassignmentCasesForm();
    caseReassignmentCasesForm.setSelectedApplicationIds(List.of("71", "52"));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(caseReassignmentCasesValidator, caseReassignmentCasesForm);
    assertThat(errorsMap).isEmpty();
  }
}
