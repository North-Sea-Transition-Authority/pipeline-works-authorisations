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
public class CaseReassignmentValidatorTest {

  private CaseReassignmentValidator caseReassignmentValidator;

  @Before
  public void setup() {
    caseReassignmentValidator = new CaseReassignmentValidator();
  }

  @Test
  public void supports_CaseReassignmentForm() {
    assertTrue(caseReassignmentValidator.supports(CaseReassignmentSelectorForm.class));
    assertFalse(caseReassignmentValidator.supports(CaseReassignmentFilterForm.class));
  }

  @Test
  public void validator_allEmpty() {
    var caseReassignmentSelectorForm = new CaseReassignmentSelectorForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(caseReassignmentValidator, caseReassignmentSelectorForm);
    assertThat(errorsMap).isNotEmpty();
    assertThat(errorsMap).contains(entry("selectedApplicationIds", Set.of("selectedApplicationIds" + FieldValidationErrorCodes.REQUIRED.getCode())));
    assertThat(errorsMap).contains(entry("assignedCaseOfficerPersonId", Set.of("assignedCaseOfficerPersonId" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validator_allValid() {
    var caseReassignmentSelectorForm = new CaseReassignmentSelectorForm();
    caseReassignmentSelectorForm.setSelectedApplicationIds(List.of("71", "52"));
    caseReassignmentSelectorForm.setAssignedCaseOfficerPersonId(1000);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(caseReassignmentValidator, caseReassignmentSelectorForm);
    assertThat(errorsMap).isEmpty();
  }
}
