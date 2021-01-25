package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.withdraw.WithdrawApplicationForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class WithdrawApplicationValidatorTest {

  private WithdrawApplicationValidator validator;
  private PwaApplication pwaApplication;

  @Before
  public void setUp() {
    validator = new WithdrawApplicationValidator();
    pwaApplication = new PwaApplication();
  }

  @Test
  public void validate_form_empty() {
    var form = new WithdrawApplicationForm();

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplication);

    assertThat(errorsMap).containsOnly(
        entry("withdrawalReason", Set.of("withdrawalReason" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_form_valid() {
    var form = new WithdrawApplicationForm();
    form.setWithdrawalReason("reason");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplication);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void validate_withdrawalReason_invalidLength() {
    var form = new WithdrawApplicationForm();
    form.setWithdrawalReason(ValidatorTestUtils.over4000Chars());

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplication);
    assertThat(errorsMap).containsOnly(
        entry("withdrawalReason", Set.of("withdrawalReason" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }


}
