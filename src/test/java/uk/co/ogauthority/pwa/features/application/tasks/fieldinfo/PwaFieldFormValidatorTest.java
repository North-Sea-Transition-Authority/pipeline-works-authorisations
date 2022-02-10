package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.INVALID;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaFieldFormValidatorTest {

  @Mock
  private DevukFieldService devukFieldService;

  private PwaFieldFormValidator validator;
  private PwaFieldForm form;

  @Before
  public void setUp() {
    var serviceNameAcronym = "PWA";
    validator = new PwaFieldFormValidator(devukFieldService, serviceNameAcronym);
    form = new PwaFieldForm();
  }

  @Test
  public void full_linkedToField_null_fail() {

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errors).containsOnlyKeys("linkedToField");
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

    assertThat(errors).containsOnlyKeys("fieldIds");
    assertThat(errors.get("fieldIds")).containsOnly(REQUIRED.errorCode("fieldIds"));

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
    form.setFieldIds(List.of("1"));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errors).isEmpty();

  }

  @Test
  public void partial_linkedToField_true_fieldId_valid_pass() {

    form.setLinkedToField(true);
    form.setFieldIds(List.of("1"));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errors).isEmpty();

  }

  @Test
  public void full_linkedToField_true_fieldId_invalid_fail() {

    form.setLinkedToField(true);
    form.setFieldIds(List.of("1"));

    when(devukFieldService.getLinkedAndManualFieldEntries(List.of("1"))).thenThrow(new PwaEntityNotFoundException("not found"));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errors).containsOnlyKeys("fieldIds");
    assertThat(errors.get("fieldIds")).containsOnly(INVALID.errorCode("fieldIds"));

  }

  @Test
  public void partial_linkedToField_true_fieldId_invalid_fail() {

    form.setLinkedToField(true);
    form.setFieldIds(List.of("1"));

    when(devukFieldService.getLinkedAndManualFieldEntries(List.of("1"))).thenThrow(new PwaEntityNotFoundException("not found"));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errors).containsOnlyKeys("fieldIds");
    assertThat(errors.get("fieldIds")).containsOnly(INVALID.errorCode("fieldIds"));

  }

  @Test
  public void full_linkedToField_false_noLinkedFieldDescription_null_fail() {

    form.setLinkedToField(false);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errors).containsOnlyKeys("noLinkedFieldDescription");
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
  public void full_linkedToField_false_noLinkedFieldDescriptionOverMaxCharLength_fail() {

    form.setLinkedToField(false);
    form.setNoLinkedFieldDescription(ValidatorTestUtils.overMaxDefaultCharLength());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errors).containsOnly(
        entry("noLinkedFieldDescription", Set.of(MAX_LENGTH_EXCEEDED.errorCode("noLinkedFieldDescription"))));

  }

  @Test
  public void partial_linkedToField_false_noLinkedFieldDescriptionOverMaxCharLength_fail() {

    form.setLinkedToField(false);
    form.setNoLinkedFieldDescription(ValidatorTestUtils.overMaxDefaultCharLength());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errors).containsOnly(
        entry("noLinkedFieldDescription", Set.of(MAX_LENGTH_EXCEEDED.errorCode("noLinkedFieldDescription"))));

  }

  @Test
  public void partial_linkedToField_false_noLinkedFieldDescription_pass() {

    form.setLinkedToField(false);
    form.setNoLinkedFieldDescription("description");

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errors).isEmpty();

  }

}