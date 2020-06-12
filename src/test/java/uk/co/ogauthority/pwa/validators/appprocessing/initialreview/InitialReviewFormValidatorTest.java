package uk.co.ogauthority.pwa.validators.appprocessing.initialreview;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.form.appprocessing.initialreview.InitialReviewForm;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class InitialReviewFormValidatorTest {

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  private InitialReviewFormValidator validator;

  @Before
  public void setUp() {
    validator = new InitialReviewFormValidator(workflowAssignmentService);
  }

  @Test
  public void validate_success() {

    var caseOfficerPerson = new Person(1, null, null, null, null);

    when(workflowAssignmentService.getAssignmentCandidates(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)).thenReturn(Set.of(caseOfficerPerson));

    var form = new InitialReviewForm();
    form.setCaseOfficerPersonId(1);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).isEmpty();

  }

  @Test
  public void valid_fail_mandatory() {

    var form = new InitialReviewForm();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).containsOnly(
        entry("caseOfficerPersonId", Set.of("caseOfficerPersonId.required"))
    );

  }

  @Test
  public void valid_fail_userNotCaseOfficer() {

    var form = new InitialReviewForm();
    form.setCaseOfficerPersonId(99);

    when(workflowAssignmentService.getAssignmentCandidates(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)).thenReturn(Set.of(new Person(1, null, null, null, null)));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).containsOnly(
        entry("caseOfficerPersonId", Set.of("caseOfficerPersonId.invalid"))
    );

  }

}
