package uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;


@ExtendWith(MockitoExtension.class)
class ConfirmOptionFormValidatorTest {

  private static final String TYPE_ATTR = "confirmedOptionType";
  private static final String OPTION_DESC_ATTR = "optionCompletedDescription";
  private static final String OTHER_DESC_ATTR = "otherWorkDescription";


  private ConfirmOptionFormValidator validator;
  private ConfirmOptionForm form;

  @BeforeEach
  void setUp() throws Exception {
    validator = new ConfirmOptionFormValidator();

    form = new ConfirmOptionForm();
  }

  @Test
  void validate_full_optionWorkComplete_tooLongDesc() {
    form.setConfirmedOptionType(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);
    form.setOptionCompletedDescription(ValidatorTestUtils.overMaxDefaultCharLength());

    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(results).containsOnly(
        entry(OPTION_DESC_ATTR, Set.of(MAX_LENGTH_EXCEEDED.errorCode(OPTION_DESC_ATTR)))
    );

  }

  @Test
  void validate_full_otherWorkComplete_tooLongDesc() {
    form.setConfirmedOptionType(ConfirmedOptionType.WORK_DONE_BUT_NOT_PRESENTED_AS_OPTION);
    form.setOtherWorkDescription(ValidatorTestUtils.overMaxDefaultCharLength());

    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(results).containsOnly(
        entry(OTHER_DESC_ATTR, Set.of(MAX_LENGTH_EXCEEDED.errorCode(OTHER_DESC_ATTR)))
    );

  }

  @Test
  void validate_full_optionWorkComplete() {
    form.setConfirmedOptionType(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);
    form.setOptionCompletedDescription(ValidatorTestUtils.exactlyMaxDefaultCharLength());

    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(results).isEmpty();

  }

  @Test
  void validate_full_otherWorkComplete() {
    form.setConfirmedOptionType(ConfirmedOptionType.WORK_DONE_BUT_NOT_PRESENTED_AS_OPTION);
    form.setOtherWorkDescription(ValidatorTestUtils.exactlyMaxDefaultCharLength());

    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(results).isEmpty();

  }

  @Test
  void validate_full_nullType() {

    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(results).containsOnly(
        entry(TYPE_ATTR, Set.of(REQUIRED.errorCode(TYPE_ATTR)))
    );

  }

  @Test
  void validate_full_noDescRequiredType() {

    var noDescRequiredTypes = EnumSet.complementOf(
        EnumSet.of(
            ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS,
            ConfirmedOptionType.WORK_DONE_BUT_NOT_PRESENTED_AS_OPTION
        )
    );

    for(ConfirmedOptionType noDescRequiredOptionType: noDescRequiredTypes){
      form.setConfirmedOptionType(noDescRequiredOptionType);
      var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

      assertThat(results).isEmpty();
    }

  }

  @Test
  void validate_partial_allNull() {
    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(results).isEmpty();

  }

  @Test
  void validate_partial_workComplete_tooLongDesc() {
    form.setConfirmedOptionType(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);
    form.setOptionCompletedDescription(ValidatorTestUtils.overMaxDefaultCharLength());

    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(results).containsOnly(
        entry(OPTION_DESC_ATTR, Set.of(MAX_LENGTH_EXCEEDED.errorCode(OPTION_DESC_ATTR)))
    );

  }

  @Test
  void validate_partial_workComplete_nullDesc() {
    form.setConfirmedOptionType(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);

    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(results).isEmpty();

  }


  @Test
  void supports_whenNotSupported() {
    assertThat(validator.supports(Object.class)).isFalse();
  }

  @Test
  void supports_whenSupported() {
    assertThat(validator.supports(ConfirmOptionForm.class)).isTrue();
  }
}