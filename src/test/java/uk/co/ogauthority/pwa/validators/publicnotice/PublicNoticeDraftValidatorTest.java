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
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.fileupload.FileUploadTestUtil;

@ExtendWith(MockitoExtension.class)
class PublicNoticeDraftValidatorTest {

  private PublicNoticeDraftValidator validator;

  private UploadFileWithDescriptionForm uploadedFileForm;

  @BeforeEach
  void setUp() {
    validator = new PublicNoticeDraftValidator();
    uploadedFileForm = FileUploadTestUtil.createDefaultUploadFileForm();
  }


  @Test
  void validate_form_empty() {
    var form = new PublicNoticeDraftForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).containsOnly(
        entry("coverLetterText", Set.of("coverLetterText" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MIN_FILE_COUNT_NOT_REACHED.getCode())),
        entry("reason", Set.of("reason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_form_valid() {
    var form = new PublicNoticeDraftForm();
    form.setCoverLetterText("text");
    form.setUploadedFileWithDescriptionForms(List.of(uploadedFileForm));
    form.setReason(PublicNoticeRequestReason.ALL_CONSULTEES_CONTENT);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_form_fileCountExceeded() {
    var form = new PublicNoticeDraftForm();
    form.setUploadedFileWithDescriptionForms(List.of(uploadedFileForm, uploadedFileForm));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT.getCode()))
    );
  }

  @Test
  void validate_form_fileDescriptionNull_invalid() {
    var form = new PublicNoticeDraftForm();
    FileUploadTestUtil.addUploadFileWithoutDescriptionToForm(form);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_form_fileDescriptionOverMaxCharLength_invalid() {
    var form = new PublicNoticeDraftForm();
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
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