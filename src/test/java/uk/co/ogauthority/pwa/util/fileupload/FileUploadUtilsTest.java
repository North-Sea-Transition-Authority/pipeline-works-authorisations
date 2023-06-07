package uk.co.ogauthority.pwa.util.fileupload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.features.application.tasks.optionstemplate.OptionsTemplateForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.FileUploadUtils;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;

public class FileUploadUtilsTest {

  private static final UploadFileWithDescriptionForm uploadedFileForm =
      new UploadFileWithDescriptionForm("1", "description", Instant.now());

  @Test
  public void validateMaxFileLimit_aboveLimit_error() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of(uploadedFileForm, uploadedFileForm));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateMaxFileLimit(form, bindingResult, 1, "Error message");

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms.exceedsMaximumFileUploadCount")));

  }

  @Test
  public void validateMaxFileLimit_emptyFileAndValidFile_emptyFileIsExcluded() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm(), uploadedFileForm));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateMaxFileLimit(form, bindingResult, 1, "Error message");

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validateMaxFileLimit_onLimit_noError() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of(uploadedFileForm));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateMaxFileLimit(form, bindingResult, 1, "Error message");

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validateMaxFileLimit_belowLimit_noError() {

    var form = new OptionsTemplateForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateMaxFileLimit(form, bindingResult, 1, "Error message");

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validateFileUploaded_multipleUploaded_noError() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of(
        new UploadFileWithDescriptionForm("1", "d", Instant.now()),
        new UploadFileWithDescriptionForm("2", "d2", Instant.now()))
    );

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateFileUploaded(form, bindingResult, true, "Error message");

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validateFileUploaded_fileUploaded_noError() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "d", Instant.now())));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateFileUploaded(form, bindingResult, true, "Error message");

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validateFileUploaded_noneUploaded_error() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of());

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateFileUploaded(form, bindingResult, true, "Error message");

    assertThat(bindingResult.hasErrors()).isTrue();

  }

  @Test
  public void validateFiles_optional_noFiles_ok() {

    var form = new OptionsTemplateForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateFiles(form, bindingResult, List.of());

    assertThat(bindingResult.hasErrors()).isFalse();


  }

  @Test
  public void validateFiles_optional_filesWithDescription_ok() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "d", Instant.now())));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateFiles(form, bindingResult, List.of());

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validateFiles_optional_filesNoDescription_errors() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", null, Instant.now())));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateFiles(form, bindingResult, List.of());

    assertThat(bindingResult.hasErrors()).isTrue();

  }

  @Test
  public void validateFiles_mandatory_noFiles_error() {

    var form = new OptionsTemplateForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateFiles(form, bindingResult, List.of(MandatoryUploadValidation.class));

    assertThat(bindingResult.hasErrors()).isTrue();

  }

  @Test
  public void validateFiles_mandatory_filesWithDescription_ok() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "d2", Instant.now())));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateFiles(form, bindingResult, List.of(MandatoryUploadValidation.class));

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validateFiles_mandatory_filesWithDescription_andDeletedFiles_ok() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of(
        new UploadFileWithDescriptionForm("1", "d2", Instant.now()),
        new UploadFileWithDescriptionForm(null, null, null))
    );

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateFiles(form, bindingResult, List.of(MandatoryUploadValidation.class));

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validateFiles_fileDescriptionOverMaxCharLength_error() {

    var form = new OptionsTemplateForm();
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateFiles(form, bindingResult, List.of());

    var fieldErrors = ValidatorTestUtils.extractErrors(bindingResult);
    assertThat(fieldErrors).contains(
        Map.entry(FileUploadTestUtil.getUploadedFileDescriptionFieldPath(0),
            Set.of(FileUploadTestUtil.getUploadedFileDescriptionFieldPath(0) + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())),
        Map.entry(FileUploadTestUtil.getUploadedFileDescriptionFieldPath(1),
            Set.of(FileUploadTestUtil.getUploadedFileDescriptionFieldPath(1) + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );

  }

  @Test
  public void validateFiles_mandatory_filesWithoutDescription_andDeletedFiles_error() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of(
        new UploadFileWithDescriptionForm(null, null, null),
        new UploadFileWithDescriptionForm("1", null, Instant.now()))
    );

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateFiles(form, bindingResult, List.of(MandatoryUploadValidation.class));

    assertThat(bindingResult.getFieldError("uploadedFileWithDescriptionForms[1].uploadedFileDescription")).isNotNull();

    String errorCode = Objects.requireNonNull(bindingResult.getFieldError("uploadedFileWithDescriptionForms[1].uploadedFileDescription")).getCode();
    assertThat(errorCode).isEqualTo("uploadedFileWithDescriptionForms[1].uploadedFileDescription.required");

  }



  @Test
  public void validateFilesDescriptionLength_fileDescriptionOverMaxCharLength_error() {

    var form = new OptionsTemplateForm();
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateFilesDescriptionLength(form, bindingResult);

    var fieldErrors = ValidatorTestUtils.extractErrors(bindingResult);
    assertThat(fieldErrors).contains(
        Map.entry(FileUploadTestUtil.getUploadedFileDescriptionFieldPath(0),
            Set.of(FileUploadTestUtil.getUploadedFileDescriptionFieldPath(0) + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())),
        Map.entry(FileUploadTestUtil.getUploadedFileDescriptionFieldPath(1),
            Set.of(FileUploadTestUtil.getUploadedFileDescriptionFieldPath(1) + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );

  }

}