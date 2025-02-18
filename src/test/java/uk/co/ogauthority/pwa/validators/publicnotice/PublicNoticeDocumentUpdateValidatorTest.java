package uk.co.ogauthority.pwa.validators.publicnotice;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.fileupload.FileUploadTestUtil;

@ExtendWith(MockitoExtension.class)
class PublicNoticeDocumentUpdateValidatorTest {

  private PublicNoticeDocumentUpdateValidator validator;

  private UploadFileWithDescriptionForm uploadedFileForm;

  @BeforeEach
  void setUp() {
    validator = new PublicNoticeDocumentUpdateValidator();
    uploadedFileForm = FileUploadTestUtil.createDefaultUploadFileForm();
  }


  @Test
  void validate_form_empty() {
    var form = new UpdatePublicNoticeDocumentForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).containsOnly(
        entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MIN_FILE_COUNT_NOT_REACHED.getCode()))
    );
  }

  @Test
  void validate_form_valid() {
    var form = new UpdatePublicNoticeDocumentForm();
    form.setUploadedFileWithDescriptionForms(List.of(uploadedFileForm));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_form_fileCountExceeded() {
    var form = new UpdatePublicNoticeDocumentForm();
    form.setUploadedFileWithDescriptionForms(List.of(uploadedFileForm, uploadedFileForm));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT.getCode()))
    );
  }


  @Test
  void validate_documentUploaded_noDescription() {
    var form = new UpdatePublicNoticeDocumentForm();
    form.setUploadedFileWithDescriptionForms(List.of(
        new UploadFileWithDescriptionForm("1", null, Instant.now())
    ));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("uploadedFileWithDescriptionForms[0].uploadedFileDescription",
            Set.of("uploadedFileWithDescriptionForms[0].uploadedFileDescription" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_form_fileDescriptionNull_invalid() {
    var form = new UpdatePublicNoticeDocumentForm();
    FileUploadTestUtil.addUploadFileWithoutDescriptionToForm(form);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_form_fileDescriptionOverMaxCharLength_invalid() {
    var form = new UpdatePublicNoticeDocumentForm();
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }









}