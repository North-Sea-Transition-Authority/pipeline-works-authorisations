package uk.co.ogauthority.pwa.validators.appprocessing.confirmsatisfactory;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.appprocessing.confirmsatisfactory.ConfirmSatisfactoryApplicationForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;


@RunWith(MockitoJUnitRunner.class)
public class ConfirmSatisfactoryApplicationFormValidatorTest {

  private ConfirmSatisfactoryApplicationFormValidator validator;

  private ConfirmSatisfactoryApplicationForm form;

  @Before
  public void setUp() throws Exception {
    validator = new ConfirmSatisfactoryApplicationFormValidator();
    form = new ConfirmSatisfactoryApplicationForm();
  }

  @Test
  public void supports_whenValidTarget() {
    assertThat(validator.supports(ConfirmSatisfactoryApplicationForm.class)).isTrue();
  }

  @Test
  public void supports_whenInvalidTarget() {
    assertThat(validator.supports(Object.class)).isFalse();
  }

  @Test
  public void validate_whenAllNull() {

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(result).isEmpty();

  }

  @Test
  public void validate_whenReasonProvided() {

    form.setReason("reason");

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(result).isEmpty();

  }

  @Test
  public void validate_whenReasonProvided_tooBig() {

    form.setReason(ValidatorTestUtils.overMaxDefaultCharLength());

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(result).containsOnly(entry("reason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("reason"))));

  }

}