package uk.co.ogauthority.pwa.features.application.tasks.partnerletters;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.fileupload.FileUploadTestUtil;

@ExtendWith(MockitoExtension.class)
class PartnerLettersValidatorTest {

  private PartnerLettersValidator validator;

  @BeforeEach
  void setUp() {
    validator = new PartnerLettersValidator();
  }


  @Test
  void validate_full_form_valid() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(true);
    FileUploadTestUtil.addDefaultUploadFileToForm(form);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_full_form_empty() {
    var form = new PartnerLettersForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("partnerLettersRequired", Set.of("partnerLettersRequired.required")));
  }

  @Test
  void validate_full_lettersRequired_invalid() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(null);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("partnerLettersConfirmed", Set.of("partnerLettersConfirmed.required")),
        entry("uploadedFileWithDescriptionForms", Set.of(FieldValidationErrorCodes.MIN_FILE_COUNT_NOT_REACHED.errorCode("uploadedFileWithDescriptionForms")))
    );
  }

  @Test
  void validate_full_letterDescription_empty() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(true);
    FileUploadTestUtil.addUploadFileWithoutDescriptionToForm(form);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(), Set.of(
            FieldValidationErrorCodes.REQUIRED.errorCode(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath())))
    );
  }

  @Test
  void validate_full_letterDescriptionOverMaxCharLength_invalid() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }

  @Test
  void validate_partial_letterDescriptionOverMaxCharLength_invalid() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);
    assertThat(errorsMap).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }







}