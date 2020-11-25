package uk.co.ogauthority.pwa.validators.options;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.util.EnumSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.options.ConfirmOptionForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;


@RunWith(MockitoJUnitRunner.class)
public class ConfirmOptionFormValidatorTest {

  private static final String TYPE_ATTR = "confirmedOptionType";
  private static final String DESC_ATTR = "optionCompletedDescription";


  private ConfirmOptionFormValidator validator;
  private ConfirmOptionForm form;

  @Before
  public void setUp() throws Exception {
    validator = new ConfirmOptionFormValidator();

    form = new ConfirmOptionForm();
  }

  @Test
  public void validate_full_workComplete_tooLongDesc() {
    form.setConfirmedOptionType(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);
    form.setOptionCompletedDescription(ValidatorTestUtils.over4000Chars());

    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(results).containsOnly(
        entry(DESC_ATTR, Set.of(MAX_LENGTH_EXCEEDED.errorCode(DESC_ATTR)))
    );

  }

  @Test
  public void validate_full_workComplete() {
    form.setConfirmedOptionType(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);
    form.setOptionCompletedDescription(ValidatorTestUtils.exactly4000chars());

    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(results).isEmpty();


  }

  @Test
  public void validate_full_nullType() {

    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(results).containsOnly(
        entry(TYPE_ATTR, Set.of(REQUIRED.errorCode(TYPE_ATTR)))
    );

  }

  @Test
  public void validate_full_noDescRequiredType() {

    var noDescRequiredTypes = EnumSet.complementOf(EnumSet.of(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS));

    for(ConfirmedOptionType noDescRequiredOptionType: noDescRequiredTypes){
      form.setConfirmedOptionType(noDescRequiredOptionType);
      var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

      assertThat(results).isEmpty();
    }

  }

  @Test
  public void validate_partial_allNull() {
    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(results).isEmpty();

  }

  @Test
  public void validate_partial_workComplete_tooLongDesc() {
    form.setConfirmedOptionType(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);
    form.setOptionCompletedDescription(ValidatorTestUtils.over4000Chars());

    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(results).containsOnly(
        entry(DESC_ATTR, Set.of(MAX_LENGTH_EXCEEDED.errorCode(DESC_ATTR)))
    );

  }

  @Test
  public void validate_partial_workComplete_nullDesc() {
    form.setConfirmedOptionType(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);

    var results = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(results).isEmpty();

  }


  @Test
  public void supports_whenNotSupported() {
    assertThat(validator.supports(Object.class)).isFalse();
  }

  @Test
  public void supports_whenSupported() {
    assertThat(validator.supports(ConfirmOptionForm.class)).isTrue();
  }
}