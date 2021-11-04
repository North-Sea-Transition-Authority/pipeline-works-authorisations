package uk.co.ogauthority.pwa.features.application.tasks.partnerletters;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PartnerLettersValidatorTest {

  private PartnerLettersValidator validator;

  @Before
  public void setUp() {
    validator = new PartnerLettersValidator();
  }


  @Test
  public void validate_form_valid() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(true);
    var uploadedFile = new UploadFileWithDescriptionForm();
    uploadedFile.setUploadedFileDescription("description");
    form.setUploadedFileWithDescriptionForms(List.of(uploadedFile));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void validate_form_empty() {
    var form = new PartnerLettersForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("partnerLettersRequired", Set.of("partnerLettersRequired.required")));
  }

  @Test
  public void validate_lettersRequired_invalid() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(null);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("partnerLettersConfirmed", Set.of("partnerLettersConfirmed.required")),
        entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms.required"))
    );
  }

  @Test
  public void validate_letterDescription_empty() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(true);
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm()));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("uploadedFileWithDescriptionForms[0].uploadedFileDescription", Set.of("uploadedFileWithDescriptionForms[0].uploadedFileDescription.required"))
    );
  }







}