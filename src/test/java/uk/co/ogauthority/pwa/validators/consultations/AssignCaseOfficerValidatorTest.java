package uk.co.ogauthority.pwa.validators.consultations;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.form.consultation.AssignCaseOfficerForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class AssignCaseOfficerValidatorTest {

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;
  private AssignCaseOfficerValidator validator;
  private PwaApplication pwaApplication;

  @BeforeEach
  void setUp() {
    validator = new AssignCaseOfficerValidator(workflowAssignmentService);
    pwaApplication = new PwaApplication();
  }


  @Test
  void validate_form_empty() {
    var form = new AssignCaseOfficerForm();

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplication);

    assertThat(errorsMap).containsOnly(
        entry("caseOfficerPersonId", Set.of("caseOfficerPersonId" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_form_valid() {
    var form = new AssignCaseOfficerForm();
    form.setCaseOfficerPersonId(1);

    var validCaseOfficerCandidatePerson = new Person(1, null, null, null, null);
    when(workflowAssignmentService.getAssignmentCandidates(any(), any())).thenReturn(Set.of(validCaseOfficerCandidatePerson));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplication);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_selectedCaseOfficer_invalid() {
    var form = new AssignCaseOfficerForm();
    form.setCaseOfficerPersonId(2);

    var validCaseOfficerCandidatePerson = new Person(1, null, null, null, null);
    when(workflowAssignmentService.getAssignmentCandidates(any(), any())).thenReturn(Set.of(validCaseOfficerCandidatePerson));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplication);
    assertThat(errorsMap).containsOnly(
        entry("caseOfficerPersonId", Set.of("caseOfficerPersonId" + FieldValidationErrorCodes.INVALID.getCode())));
  }







}