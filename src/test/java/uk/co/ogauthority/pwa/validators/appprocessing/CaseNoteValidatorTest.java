package uk.co.ogauthority.pwa.validators.appprocessing;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.form.appprocessing.casenotes.AddCaseNoteForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.fileupload.FileUploadTestUtil;
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
    FileUploadTestUtil.addDefaultUploadFileToForm(form);

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
    FileUploadTestUtil.addUploadFileWithoutDescriptionToForm(form);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(), Set.of(
            FieldValidationErrorCodes.REQUIRED.errorCode(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath())))
    );
  }

  @Test
  void validate_fileDescriptionOverMaxCharLength_invalid() {
    var form = new AddCaseNoteForm();
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }




}