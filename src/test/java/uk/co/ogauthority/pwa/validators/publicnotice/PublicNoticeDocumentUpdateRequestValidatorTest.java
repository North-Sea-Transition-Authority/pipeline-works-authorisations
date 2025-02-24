package uk.co.ogauthority.pwa.validators.publicnotice;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDocumentUpdateRequestForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class PublicNoticeDocumentUpdateRequestValidatorTest {

  private PublicNoticeDocumentUpdateRequestValidator validator;


  @BeforeEach
  void setUp() {
    validator = new PublicNoticeDocumentUpdateRequestValidator();
  }


  @Test
  void validate_form_empty() {
    var form = new PublicNoticeDocumentUpdateRequestForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).containsOnly(
        entry("comments", Set.of("comments" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_form_valid() {
    var form = new PublicNoticeDocumentUpdateRequestForm();
    form.setComments("some comments");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_form_textAreaLengthExceeded() {
    var form = new PublicNoticeDocumentUpdateRequestForm();
    form.setComments(ValidatorTestUtils.overMaxDefaultCharLength());
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("comments", Set.of("comments" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }




}