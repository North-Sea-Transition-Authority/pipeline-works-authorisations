package uk.co.ogauthority.pwa.validators.publicnotice;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PublicNoticeDocumentUpdateValidatorTest {

  private PublicNoticeDocumentUpdateValidator validator;

  private UploadFileWithDescriptionForm uploadedFileForm;

  @Before
  public void setUp() {
    validator = new PublicNoticeDocumentUpdateValidator();
    uploadedFileForm = new UploadFileWithDescriptionForm();
    uploadedFileForm.setUploadedFileId("file id 1");
    uploadedFileForm.setUploadedFileDescription("description");
  }


  @Test
  public void validate_form_empty() {
    var form = new UpdatePublicNoticeDocumentForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).containsOnly(
        entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MIN_FILE_COUNT_NOT_REACHED.getCode()))
    );
  }

  @Test
  public void validate_form_valid() {
    var form = new UpdatePublicNoticeDocumentForm();
    form.setUploadedFileWithDescriptionForms(List.of(uploadedFileForm));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void validate_form_fileCountExceeded() {
    var form = new UpdatePublicNoticeDocumentForm();
    form.setUploadedFileWithDescriptionForms(List.of(uploadedFileForm, uploadedFileForm));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT.getCode()))
    );
  }


  @Test
  public void validate_documentUploaded_noDescription() {
    var form = new UpdatePublicNoticeDocumentForm();
    form.setUploadedFileWithDescriptionForms(List.of(
        new UploadFileWithDescriptionForm("1", null, Instant.now())
    ));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("uploadedFileWithDescriptionForms[0].uploadedFileDescription",
            Set.of("uploadedFileWithDescriptionForms[0].uploadedFileDescription" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }









}