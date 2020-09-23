package uk.co.ogauthority.pwa.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.options.OptionsTemplateForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

public class FileUploadUtilsTest {

  @Test
  public void validateMaxFileLimit_aboveLimit_error() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm(), new UploadFileWithDescriptionForm()));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    FileUploadUtils.validateMaxFileLimit(form, bindingResult, 1, "Error message");

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms.exceedsMaximumFileUploadCount")));

  }

  @Test
  public void validateMaxFileLimit_onLimit_noError() {

    var form = new OptionsTemplateForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm()));

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

}