package uk.co.ogauthority.pwa.service.pwaapplications.initial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.INVALID;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.form.pwaapplications.fields.PwaFieldForm;
import uk.co.ogauthority.pwa.service.devuk.DevukFieldService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.PwaFieldFormValidator;

@RunWith(MockitoJUnitRunner.class)
public class PwaFieldFormValidatorTest {

  @Mock
  private DevukFieldService devukFieldService;

  private PwaFieldFormValidator validator;
  private PwaFieldForm form;

  @Before
  public void setUp() {
    validator = new PwaFieldFormValidator(devukFieldService);
    form = new PwaFieldForm();
  }

  @Test
  public void full_linkedtoField_null_fail() {

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errors.keySet()).containsOnly("linkedToField");
    assertThat(errors.get("linkedToField")).containsOnly(REQUIRED.errorCode("linkedToField"));

  }

  @Test
  public void partial_linkedToField_null_pass() {

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errors).isEmpty();

  }

  @Test
  public void full_linkedToField_true_fieldId_null_fail() {

    form.setLinkedToField(true);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errors.keySet()).containsOnly("fieldId");
    assertThat(errors.get("fieldId")).containsOnly(REQUIRED.errorCode("fieldId"));

  }

  @Test
  public void partial_linkedToField_true_fieldId_null_pass() {

    form.setLinkedToField(true);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errors).isEmpty();

  }

  @Test
  public void full_linkedToField_true_fieldId_valid_pass() {

    form.setLinkedToField(true);
    form.setFieldId(1);

    when(devukFieldService.findById(1)).thenReturn(new DevukField());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errors).isEmpty();

  }

  @Test
  public void partial_linkedToField_true_fieldId_valid_pass() {

    form.setLinkedToField(true);
    form.setFieldId(1);

    when(devukFieldService.findById(1)).thenReturn(new DevukField());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errors).isEmpty();

  }

  @Test
  public void full_linkedToField_true_fieldId_invalid_fail() {

    form.setLinkedToField(true);
    form.setFieldId(1);

    when(devukFieldService.findById(1)).thenThrow(new PwaEntityNotFoundException("not found"));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errors.keySet()).containsOnly("fieldId");
    assertThat(errors.get("fieldId")).containsOnly(INVALID.errorCode("fieldId"));

  }

  @Test
  public void partial_linkedToField_true_fieldId_invalid_fail() {

    form.setLinkedToField(true);
    form.setFieldId(1);

    when(devukFieldService.findById(1)).thenThrow(new PwaEntityNotFoundException("not found"));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errors.keySet()).containsOnly("fieldId");
    assertThat(errors.get("fieldId")).containsOnly(INVALID.errorCode("fieldId"));

  }

  @Test
  public void full_linkedToField_false_noLinkedFieldDescription_null_fail() {

    form.setLinkedToField(false);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errors.keySet()).containsOnly("noLinkedFieldDescription");
    assertThat(errors.get("noLinkedFieldDescription")).containsOnly(REQUIRED.errorCode("noLinkedFieldDescription"));

  }

  @Test
  public void partial_linkedToField_false_noLinkedFieldDescription_null_pass() {

    form.setLinkedToField(false);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errors).isEmpty();

  }

  @Test
  public void full_linkedToField_false_noLinkedFieldDescription_pass() {

    form.setLinkedToField(false);
    form.setNoLinkedFieldDescription("description");

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errors).isEmpty();

  }

  @Test
  public void partial_linkedToField_false_noLinkedFieldDescription_pass() {

    form.setLinkedToField(false);
    form.setNoLinkedFieldDescription("description");

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errors).isEmpty();

  }

}