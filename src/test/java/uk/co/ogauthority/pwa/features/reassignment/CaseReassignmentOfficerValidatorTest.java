package uk.co.ogauthority.pwa.features.reassignment;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class CaseReassignmentOfficerValidatorTest {

  private CaseReassignmentOfficerValidator caseReassignmentOfficerValidator;

  @Before
  public void setup() {
    caseReassignmentOfficerValidator = new CaseReassignmentOfficerValidator();
  }

  @Test
  public void supports_CaseReassignmentForm() {
    assertTrue(caseReassignmentOfficerValidator.supports(CaseReassignmentOfficerForm.class));
    assertFalse(caseReassignmentOfficerValidator.supports(CaseReassignmentFilterForm.class));
  }

  @Test
  public void validator_allEmpty() {
    var caseReassignmentOfficerForm = new CaseReassignmentOfficerForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(caseReassignmentOfficerValidator, caseReassignmentOfficerForm);
    assertThat(errorsMap).isNotEmpty();
    assertThat(errorsMap).contains(entry("assignedCaseOfficerPersonId", Set.of("assignedCaseOfficerPersonId" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validator_allValid() {
    var caseReassignmentOfficerForm = new CaseReassignmentOfficerForm();
    caseReassignmentOfficerForm.setAssignedCaseOfficerPersonId(1000);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(caseReassignmentOfficerValidator, caseReassignmentOfficerForm);
    assertThat(errorsMap).isEmpty();
  }
}
