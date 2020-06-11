package uk.co.ogauthority.pwa.service.pwaapplications.initial;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.fields.PwaFieldForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.PwaFieldFormValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadFieldFormValidatorTest {

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
    assertThat(errors.get("fieldId")).containsExactly("fieldId.required");
  }

  @Test
  public void validate_RadioYes() {
    pwaFieldForm.setLinkedToField(true);
    pwaFieldForm.setFieldId(1);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, pwaFieldForm);
    assertThat(errors.get("fieldId")).isNull();
  }

  @Test
  public void validate_RadioNo_EmptyDescription() {
    pwaFieldForm.setLinkedToField(false);
    pwaFieldForm.setNoLinkedFieldDescription(null);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, pwaFieldForm);
    assertThat(errors.get("noLinkedFieldDescription")).containsExactly("noLinkedFieldDescription.required");
  }

}