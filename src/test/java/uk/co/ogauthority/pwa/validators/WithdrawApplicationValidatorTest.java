package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.form.withdraw.WithdrawApplicationForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class WithdrawApplicationValidatorTest {

  private WithdrawApplicationValidator validator;
  private PwaApplication pwaApplication;

  @BeforeEach
  void setUp() {
    validator = new WithdrawApplicationValidator();
    pwaApplication = new PwaApplication();
  }

  @Test
  void validate_form_empty() {
    var form = new WithdrawApplicationForm();

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplication);

    assertThat(errorsMap).containsOnly(
        entry("withdrawalReason", Set.of("withdrawalReason" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_form_valid() {
    var form = new WithdrawApplicationForm();
    form.setWithdrawalReason("reason");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplication);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_withdrawalReason_invalidLength() {
    var form = new WithdrawApplicationForm();
    form.setWithdrawalReason(ValidatorTestUtils.overMaxDefaultCharLength());

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplication);
    assertThat(errorsMap).containsOnly(
        entry("withdrawalReason", Set.of("withdrawalReason" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }


}
