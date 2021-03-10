package uk.co.ogauthority.pwa.validators.publicnotice;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.publicnotice.WithdrawPublicNoticeForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class WithdrawPublicNoticeValidatorTest {

  private WithdrawPublicNoticeValidator validator;


  @Before
  public void setUp() {
    validator = new WithdrawPublicNoticeValidator();
  }


  @Test
  public void validate_form_empty() {
    var form = new WithdrawPublicNoticeForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).containsOnly(
        entry("withdrawalReason", Set.of("withdrawalReason" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_form_valid() {
    var form = new WithdrawPublicNoticeForm();
    form.setWithdrawalReason("My reason");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void validate_form_textAreaLengthExceeded() {
    var form = new WithdrawPublicNoticeForm();
    form.setWithdrawalReason(ValidatorTestUtils.over4000Chars());
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("withdrawalReason", Set.of("withdrawalReason" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }










}