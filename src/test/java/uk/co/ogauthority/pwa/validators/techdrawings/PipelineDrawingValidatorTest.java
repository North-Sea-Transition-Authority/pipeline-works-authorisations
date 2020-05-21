package uk.co.ogauthority.pwa.validators.techdrawings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

public class PipelineDrawingValidatorTest {

  private PipelineDrawingValidator validator;
  private PipelineDrawingForm form;

  @Before
  public void setUp() {
    validator = new PipelineDrawingValidator();
    form = new PipelineDrawingForm();
  }

  @Test
  public void validate_emptyForm() {
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).containsOnlyKeys("reference", "pipelineIds");
  }

  @Test
  public void validate_referenceWhitespace() {
    form.setReference(" ");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).containsOnlyKeys("reference", "pipelineIds");
  }

  @Test
  public void validate_validForm() {
    form.setPipelineIds(List.of(1));
    form.setReference("Test");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).isEmpty();
  }
}