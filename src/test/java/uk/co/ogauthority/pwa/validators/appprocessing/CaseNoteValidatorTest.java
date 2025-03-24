package uk.co.ogauthority.pwa.validators.appprocessing;

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
import uk.co.ogauthority.pwa.model.form.appprocessing.casenotes.AddCaseNoteForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.appprocessing.casenote.CaseNoteFormValidator;

@ExtendWith(MockitoExtension.class)
class CaseNoteValidatorTest {

  private CaseNoteFormValidator validator;

  @BeforeEach
  void setUp() {
    validator = new CaseNoteFormValidator();
  }


  @Test
  void validate_noteTextAdded_valid() {
    var form = new AddCaseNoteForm();
    form.setNoteText("note text");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_formComplete_valid() {
    var form = new AddCaseNoteForm();
    form.setNoteText("note text");
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_formEmpty_invalid() {
    var form = new AddCaseNoteForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("noteText",
            Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("noteText"))));
  }

  @Test
  void validate_fileDescriptionEmpty_invalid() {
    var form = new AddCaseNoteForm();
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("uploadedFiles[0].uploadedFileDescription",
            Set.of("uploadedFiles[0].uploadedFileDescription" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }
}