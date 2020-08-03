package uk.co.ogauthority.pwa.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.UmbilicalCrossSectionForm;

public class FileUploadUtilsTest {

  @Test
  public void getDropzoneErrorMessage_noErrors() {
    var bindingResult = new BeanPropertyBindingResult(new UmbilicalCrossSectionForm(), "form");
    assertThat(FileUploadUtils.getDropzoneErrorMessage(bindingResult)).isEmpty();
  }

  @Test
  public void getDropzoneErrorMessage_containsErrorsOnField() {
    var bindingResult = new BeanPropertyBindingResult(new UmbilicalCrossSectionForm(), "form");
    bindingResult.rejectValue("uploadedFileWithDescriptionForms", "uploadedFileWithDescriptionForms.err", "Test error");
    assertThat(FileUploadUtils.getDropzoneErrorMessage(bindingResult)).get().isEqualTo("Test error");
  }

}