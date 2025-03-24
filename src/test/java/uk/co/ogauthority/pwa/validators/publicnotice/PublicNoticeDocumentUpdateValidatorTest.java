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
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class PublicNoticeDocumentUpdateValidatorTest {

  private PublicNoticeDocumentUpdateValidator validator;


  @BeforeEach
  void setUp() {
    validator = new PublicNoticeDocumentUpdateValidator();
  }


  @Test
  void validate_form_empty() {
    var form = new UpdatePublicNoticeDocumentForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).containsOnly(
        entry("uploadedFiles", Set.of(FileValidationUtils.BELOW_THRESHOLD_ERROR_CODE))
    );
  }

  @Test
  void validate_form_valid() {
    var form = new UpdatePublicNoticeDocumentForm();
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_form_fileCountExceeded() {
    var form = new UpdatePublicNoticeDocumentForm();
    form.setUploadedFiles(
        List.of(
            FileManagementValidatorTestUtils.createUploadedFileForm(),
            FileManagementValidatorTestUtils.createUploadedFileForm()
        )
    );

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("uploadedFiles", Set.of(FileValidationUtils.ABOVE_LIMIT_ERROR_CODE))
    );
  }


  @Test
  void validate_documentUploaded_noDescription() {
    var form = new UpdatePublicNoticeDocumentForm();
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("uploadedFiles[0].uploadedFileDescription",
            Set.of("uploadedFiles[0].uploadedFileDescription" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }
}