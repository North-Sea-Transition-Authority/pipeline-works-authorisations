package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class ConsentReviewReturnFormValidatorTest {

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  private ConsentReviewReturnFormValidator validator;

  @BeforeEach
  void setUp() throws Exception {

    validator = new ConsentReviewReturnFormValidator(workflowAssignmentService);

  }

  @Test
  void mandatory_fail() {

    var form = new ConsentReviewReturnForm();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, new PwaApplication());

    assertThat(errors).containsExactly(
        entry("caseOfficerPersonId", Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("caseOfficerPersonId"))),
        entry("returnReason", Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("returnReason"))));

  }

  @Test
  void mandatory_pass() {

    var form = new ConsentReviewReturnForm();
    form.setCaseOfficerPersonId(2);
    form.setReturnReason("return");

    when(workflowAssignmentService.getAssignmentCandidates(any(), eq(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)))
        .thenReturn(Set.of(PersonTestUtil.createPersonFrom(new PersonId(2))));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, new PwaApplication());

    assertThat(errors).isEmpty();

  }

  @Test
  void validCaseOfficer_fail() {

    var form = new ConsentReviewReturnForm();
    form.setCaseOfficerPersonId(2);
    form.setReturnReason("return");

    when(workflowAssignmentService.getAssignmentCandidates(any(), eq(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)))
        .thenReturn(Set.of());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, new PwaApplication());

    assertThat(errors).containsOnly(entry("caseOfficerPersonId", Set.of(FieldValidationErrorCodes.INVALID.errorCode("caseOfficerPersonId"))));

  }

  @Test
  void validate_returnReasonTooLong() {

    var form = new ConsentReviewReturnForm();
    form.setReturnReason(ValidatorTestUtils.overMaxDefaultCharLength());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, new PwaApplication());
    assertThat(result).contains(
        Map.entry("returnReason", Set.of("returnReason" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

}