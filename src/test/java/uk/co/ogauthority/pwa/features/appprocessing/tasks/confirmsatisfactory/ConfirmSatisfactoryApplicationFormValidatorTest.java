package uk.co.ogauthority.pwa.features.appprocessing.tasks.confirmsatisfactory;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;


@ExtendWith(MockitoExtension.class)
class ConfirmSatisfactoryApplicationFormValidatorTest {

  private ConfirmSatisfactoryApplicationFormValidator validator;

  private ConfirmSatisfactoryApplicationForm form;

  @BeforeEach
  void setUp() throws Exception {
    validator = new ConfirmSatisfactoryApplicationFormValidator();
    form = new ConfirmSatisfactoryApplicationForm();
  }

  @Test
  void supports_whenValidTarget() {
    assertThat(validator.supports(ConfirmSatisfactoryApplicationForm.class)).isTrue();
  }

  @Test
  void supports_whenInvalidTarget() {
    assertThat(validator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenAllNull() {

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(result).isEmpty();

  }

  @Test
  void validate_whenReasonProvided() {

    form.setReason("reason");

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(result).isEmpty();

  }

  @Test
  void validate_whenReasonProvided_tooBig() {

    form.setReason(ValidatorTestUtils.overMaxDefaultCharLength());

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(result).containsOnly(entry("reason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("reason"))));

  }

}