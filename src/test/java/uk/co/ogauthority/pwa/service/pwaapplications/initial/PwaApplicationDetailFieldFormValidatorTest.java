package uk.co.ogauthority.pwa.service.pwaapplications.initial;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.fields.PwaFieldForm;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.PwaFieldFormValidator;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationDetailFieldFormValidatorTest<T> {

  private PwaFieldFormValidator validator;
  private PwaFieldForm pwaFieldForm;

  @Before
  public void setUp() {
    validator = new PwaFieldFormValidator();
    pwaFieldForm = new PwaFieldForm();
  }

  @Test
  public void validate_RadioYes_NoSelection() {
    pwaFieldForm.setLinkedToField(true);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, pwaFieldForm);
    assertThat(errors.get("fieldId")).containsExactly("fieldId.empty");
  }

  @Test
  public void validate_RadioYes() {
    pwaFieldForm.setLinkedToField(true);
    pwaFieldForm.setFieldId(1);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, pwaFieldForm);
    assertThat(errors.get("fieldId")).isNull();
  }

}