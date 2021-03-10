package uk.co.ogauthority.pwa.service.appprocessing.consentreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.appprocessing.prepareconsent.ConsentReviewReturnForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConsentReviewReturnFormValidatorTest {

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  private ConsentReviewReturnFormValidator validator;

  @Before
  public void setUp() throws Exception {

    validator = new ConsentReviewReturnFormValidator(workflowAssignmentService);

  }

  @Test
  public void mandatory_fail() {

    var form = new ConsentReviewReturnForm();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, new PwaApplication());

    assertThat(errors).containsExactly(
        entry("caseOfficerPersonId", Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("caseOfficerPersonId"))),
        entry("returnReason", Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("returnReason"))));

  }

  @Test
  public void mandatory_pass() {

    var form = new ConsentReviewReturnForm();
    form.setCaseOfficerPersonId(2);
    form.setReturnReason("return");

    when(workflowAssignmentService.getAssignmentCandidates(any(), eq(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)))
        .thenReturn(Set.of(PersonTestUtil.createPersonFrom(new PersonId(2))));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, new PwaApplication());

    assertThat(errors).isEmpty();

  }

  @Test
  public void validCaseOfficer_fail() {

    var form = new ConsentReviewReturnForm();
    form.setCaseOfficerPersonId(2);
    form.setReturnReason("return");

    when(workflowAssignmentService.getAssignmentCandidates(any(), eq(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)))
        .thenReturn(Set.of());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, new PwaApplication());

    assertThat(errors).containsOnly(entry("caseOfficerPersonId", Set.of(FieldValidationErrorCodes.INVALID.errorCode("caseOfficerPersonId"))));

  }

}