package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.INVALID;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class PwaAreaFormValidatorTest {

  @Mock
  private DevukFieldService devukFieldService;

  private PwaAreaFormValidator validator;
  private PwaAreaForm form;

  private PwaApplicationDetail applicationDetail;

  @BeforeEach
  void setUp() {
    var serviceNameAcronym = "PWA";
    validator = new PwaAreaFormValidator(devukFieldService, serviceNameAcronym);
    form = new PwaAreaForm();
    var application = new PwaApplication();
    application.setResourceType(PwaResourceType.PETROLEUM);
    applicationDetail = new PwaApplicationDetail(
        application,
        1,
        1,
        Instant.now());
  }

  @Test
  void full_linkedToField_null_fail() {

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL, applicationDetail);

    assertThat(errors).containsOnlyKeys("linkedToArea");
    assertThat(errors.get("linkedToArea")).containsOnly(REQUIRED.errorCode("linkedToArea"));

  }

  @Test
  void partial_linkedToField_null_pass() {

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL, applicationDetail);

    assertThat(errors).isEmpty();

  }

  @Test
  void full_linkedToField_true_fieldId_null_fail() {

    form.setLinkedToArea(true);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL, applicationDetail);

    assertThat(errors).containsOnlyKeys("linkedAreas");
    assertThat(errors.get("linkedAreas")).containsOnly(REQUIRED.errorCode("linkedAreas"));

  }

  @Test
  void partial_linkedToField_true_fieldId_null_pass() {

    form.setLinkedToArea(true);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL, applicationDetail);

    assertThat(errors).isEmpty();

  }

  @Test
  void full_linkedToField_true_fieldId_valid_pass() {

    form.setLinkedToArea(true);
    form.setLinkedAreas(List.of("1"));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL, applicationDetail);

    assertThat(errors).isEmpty();

  }

  @Test
  void partial_linkedToField_true_fieldId_valid_pass() {

    form.setLinkedToArea(true);
    form.setLinkedAreas(List.of("1"));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL, applicationDetail);

    assertThat(errors).isEmpty();

  }

  @Test
  void full_linkedToField_true_fieldId_invalid_fail() {

    form.setLinkedToArea(true);
    form.setLinkedAreas(List.of("1"));

    when(devukFieldService.getLinkedAndManualFieldEntries(List.of("1"))).thenThrow(new PwaEntityNotFoundException("not found"));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL, applicationDetail);

    assertThat(errors).containsOnlyKeys("linkedAreas");
    assertThat(errors.get("linkedAreas")).containsOnly(INVALID.errorCode("linkedAreas"));

  }

  @Test
  void partial_linkedToField_true_fieldId_invalid_fail() {

    form.setLinkedToArea(true);
    form.setLinkedAreas(List.of("1"));

    when(devukFieldService.getLinkedAndManualFieldEntries(List.of("1"))).thenThrow(new PwaEntityNotFoundException("not found"));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL, applicationDetail);

    assertThat(errors).containsOnlyKeys("linkedAreas");
    assertThat(errors.get("linkedAreas")).containsOnly(INVALID.errorCode("linkedAreas"));

  }

  @Test
  void full_linkedToField_false_noLinkedFieldDescription_null_fail() {

    form.setLinkedToArea(false);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL, applicationDetail);

    assertThat(errors).containsOnlyKeys("noLinkedAreaDescription");
    assertThat(errors.get("noLinkedAreaDescription")).containsOnly(REQUIRED.errorCode("noLinkedAreaDescription"));

  }

  @Test
  void partial_linkedToField_false_noLinkedFieldDescription_null_pass() {

    form.setLinkedToArea(false);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL, applicationDetail);

    assertThat(errors).isEmpty();

  }

  @Test
  void full_linkedToField_false_noLinkedFieldDescription_pass() {

    form.setLinkedToArea(false);
    form.setNoLinkedAreaDescription("description");

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL, applicationDetail);

    assertThat(errors).isEmpty();

  }

  @Test
  void full_linkedToField_false_noLinkedFieldDescriptionOverMaxCharLength_fail() {

    form.setLinkedToArea(false);
    form.setNoLinkedAreaDescription(ValidatorTestUtils.overMaxDefaultCharLength());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL, applicationDetail);

    assertThat(errors).containsOnly(
        entry("noLinkedAreaDescription", Set.of(MAX_LENGTH_EXCEEDED.errorCode("noLinkedAreaDescription"))));

  }

  @Test
  void partial_linkedToField_false_noLinkedFieldDescriptionOverMaxCharLength_fail() {

    form.setLinkedToArea(false);
    form.setNoLinkedAreaDescription(ValidatorTestUtils.overMaxDefaultCharLength());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL, applicationDetail);

    assertThat(errors).containsOnly(
        entry("noLinkedAreaDescription", Set.of(MAX_LENGTH_EXCEEDED.errorCode("noLinkedAreaDescription"))));

  }

  @Test
  void partial_linkedToField_false_noLinkedFieldDescription_pass() {

    form.setLinkedToArea(false);
    form.setNoLinkedAreaDescription("description");

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL, applicationDetail);

    assertThat(errors).isEmpty();

  }

}
