package uk.co.ogauthority.pwa.validators.publicnotice;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class PublicNoticeDraftValidatorTest {

  private PublicNoticeDraftValidator validator;

  @BeforeEach
  void setUp() {
    validator = new PublicNoticeDraftValidator();
  }

  @Test
  void validate_form_empty() {
    var form = new PublicNoticeDraftForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).containsOnly(
        entry("coverLetterText", Set.of("coverLetterText" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("uploadedFiles", Set.of(FileValidationUtils.BELOW_THRESHOLD_ERROR_CODE)),
        entry("reason", Set.of("reason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_form_valid() {
    var form = new PublicNoticeDraftForm();
    form.setCoverLetterText("text");
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    form.setReason(PublicNoticeRequestReason.ALL_CONSULTEES_CONTENT);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_form_fileCountExceeded() {
    var form = new PublicNoticeDraftForm();
    form.setUploadedFiles(List.of(
        FileManagementValidatorTestUtils.createUploadedFileForm(),
        FileManagementValidatorTestUtils.createUploadedFileForm()
    ));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("uploadedFiles", Set.of(FileValidationUtils.ABOVE_LIMIT_ERROR_CODE))
    );
  }

  @Test
  void validate_form_fileDescriptionNull_invalid() {
    var form = new PublicNoticeDraftForm();
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("uploadedFiles[0].uploadedFileDescription",
            Set.of("uploadedFiles[0].uploadedFileDescription" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_form_reasonIsNotContent_reasonDescriptionNull() {
    var form = new PublicNoticeDraftForm();
    form.setReason(PublicNoticeRequestReason.CONSULTEES_NOT_ALL_CONTENT);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("reasonDescription", Set.of("reasonDescription" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_form_textAreaLengthExceeded() {
    var form = new PublicNoticeDraftForm();
    form.setCoverLetterText(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setReason(PublicNoticeRequestReason.CONSULTEES_NOT_ALL_CONTENT);
    form.setReasonDescription(ValidatorTestUtils.overMaxDefaultCharLength());
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("coverLetterText", Set.of("coverLetterText" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())),
        entry("reasonDescription", Set.of("reasonDescription" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }
}