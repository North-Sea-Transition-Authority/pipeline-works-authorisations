package uk.co.ogauthority.pwa.features.application.tasks.partnerletters;

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
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

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
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

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
        entry("uploadedFiles", Set.of(FileValidationUtils.BELOW_THRESHOLD_ERROR_CODE))
    );
  }

  @Test
  void validate_full_letterDescription_empty() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(true);
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("uploadedFiles[0].uploadedFileDescription",
            Set.of("uploadedFiles[0].uploadedFileDescription" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }
}