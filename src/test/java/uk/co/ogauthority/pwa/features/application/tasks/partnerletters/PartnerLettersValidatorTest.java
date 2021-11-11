package uk.co.ogauthority.pwa.features.application.tasks.partnerletters;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.fileupload.FileUploadTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PartnerLettersValidatorTest {

  private PartnerLettersValidator validator;

  @Before
  public void setUp() {
    validator = new PartnerLettersValidator();
  }


  @Test
  public void validate_full_form_valid() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(true);
    FileUploadTestUtil.addDefaultUploadFileToForm(form);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void validate_full_form_empty() {
    var form = new PartnerLettersForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("partnerLettersRequired", Set.of("partnerLettersRequired.required")));
  }

  @Test
  public void validate_full_lettersRequired_invalid() {
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
  public void validate_full_letterDescription_empty() {
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
  public void validate_full_letterDescriptionOverMaxCharLength_invalid() {
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
  public void validate_partial_letterDescriptionOverMaxCharLength_invalid() {
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