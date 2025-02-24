package uk.co.ogauthority.pwa.validators.publicnotice;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeApprovalForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeApprovalResult;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class PublicNoticeApprovalValidatorTest {

  private PublicNoticeApprovalValidator validator;


  @BeforeEach
  void setUp() {
    validator = new PublicNoticeApprovalValidator();
  }


  @Test
  void validate_form_empty() {
    var form = new PublicNoticeApprovalForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).containsOnly(
        entry("requestApproved", Set.of("requestApproved" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_form_valid() {
    var form = new PublicNoticeApprovalForm();
    form.setRequestApproved(PwaApplicationPublicNoticeApprovalResult.REQUEST_APPROVED);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_form_requestRejected_reasonDescriptionNull() {
    var form = new PublicNoticeApprovalForm();
    form.setRequestApproved(PwaApplicationPublicNoticeApprovalResult.REQUEST_REJECTED);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("requestRejectedReason", Set.of("requestRejectedReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_form_textAreaLengthExceeded() {
    var form = new PublicNoticeApprovalForm();
    form.setRequestApproved(PwaApplicationPublicNoticeApprovalResult.REQUEST_REJECTED);
    form.setRequestRejectedReason(ValidatorTestUtils.overMaxDefaultCharLength());
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("requestRejectedReason", Set.of("requestRejectedReason" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }










}