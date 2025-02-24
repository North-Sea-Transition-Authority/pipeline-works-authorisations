package uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;

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
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class InitialReviewFormValidatorTest {

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  private InitialReviewFormValidator validator;

  @BeforeEach
  void setUp() {
    validator = new InitialReviewFormValidator(workflowAssignmentService);
  }

  @Test
  void validate_paymentWaived_withReason_andCaseOfficer() {

    var caseOfficerPerson = new Person(1, null, null, null, null);

    when(workflowAssignmentService.getAssignmentCandidates(any(), eq(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW))).thenReturn(Set.of(caseOfficerPerson));

    var form = new InitialReviewForm();
    form.setCaseOfficerPersonId(1);
    form.setPaymentWaivedReason("Reason");
    form.setInitialReviewPaymentDecision(InitialReviewPaymentDecision.PAYMENT_WAIVED);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, new PwaApplication());

    assertThat(errors).isEmpty();

  }

  @Test
  void validate_paymentWaivedReasonCharLengthLongerThanMax() {

    var caseOfficerPerson = new Person(1, null, null, null, null);

    when(workflowAssignmentService.getAssignmentCandidates(any(), eq(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW))).thenReturn(Set.of(caseOfficerPerson));

    var form = new InitialReviewForm();
    form.setCaseOfficerPersonId(1);
    form.setPaymentWaivedReason(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setInitialReviewPaymentDecision(InitialReviewPaymentDecision.PAYMENT_WAIVED);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, new PwaApplication());

    assertThat(errors).contains(
        Map.entry("paymentWaivedReason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("paymentWaivedReason"))));

  }

  @Test
  void validate_paymentWaived_NoReason_andCaseOfficer() {

    var caseOfficerPerson = new Person(1, null, null, null, null);

    when(workflowAssignmentService.getAssignmentCandidates(any(), eq(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW))).thenReturn(Set.of(caseOfficerPerson));

    var form = new InitialReviewForm();
    form.setCaseOfficerPersonId(1);
    form.setInitialReviewPaymentDecision(InitialReviewPaymentDecision.PAYMENT_WAIVED);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, new PwaApplication());

    assertThat(errors).containsOnly(
        entry("paymentWaivedReason", Set.of("paymentWaivedReason.required"))

    );

  }

  @Test
  void valid_fail_mandatory() {

    var form = new InitialReviewForm();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, new PwaApplication());

    assertThat(errors).containsOnly(
        entry("caseOfficerPersonId", Set.of("caseOfficerPersonId.required")),
        entry("initialReviewPaymentDecision", Set.of("initialReviewPaymentDecision.required"))

    );

  }

  @Test
  void valid_fail_userNotCaseOfficer() {

    var form = new InitialReviewForm();
    form.setInitialReviewPaymentDecision(InitialReviewPaymentDecision.PAYMENT_REQUIRED);
    form.setCaseOfficerPersonId(99);

    when(workflowAssignmentService.getAssignmentCandidates(any(), eq(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW))).thenReturn(Set.of(new Person(1, null, null, null, null)));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, new PwaApplication());

    assertThat(errors).containsOnly(
        entry("caseOfficerPersonId", Set.of("caseOfficerPersonId.invalid"))
    );

  }

}
