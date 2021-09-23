package uk.co.ogauthority.pwa.service.appprocessing.processingcharges;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.CancelAppChargeForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

public class CancelAppChargeFormValidatorTest {
  private static final String CANCEL_REASON_ATTR = "cancellationReason";

  private CancelAppChargeFormValidator cancelAppChargeFormValidator;
  private CancelAppChargeForm form;

  @Before
  public void setUp() throws Exception {
    cancelAppChargeFormValidator = new CancelAppChargeFormValidator();
    form = new CancelAppChargeForm();
  }

  @Test
  public void supports_whenValid() {
    assertThat(cancelAppChargeFormValidator.supports(CancelAppChargeForm.class)).isTrue();
  }

  @Test
  public void supports_whenInvalid() {
    assertThat(cancelAppChargeFormValidator.supports(Object.class)).isFalse();
  }

  @Test
  public void validate_whenCancelReasonNull() {
    var errors = ValidatorTestUtils.getFormValidationErrors(cancelAppChargeFormValidator, form);
    assertThat(errors).contains(
        entry(CANCEL_REASON_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(CANCEL_REASON_ATTR))));
  }

  @Test
  public void validate_whenCancelReasonOverMaxDefaultCharLength() {
    form.setCancellationReason(ValidatorTestUtils.overMaxDefaultCharLength());
    var errors = ValidatorTestUtils.getFormValidationErrors(cancelAppChargeFormValidator, form);
    assertThat(errors).contains(
        entry(CANCEL_REASON_ATTR, Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode(CANCEL_REASON_ATTR))));
  }

  @Test
  public void validate_whenCancelReasonExactlyMaxDefaultCharLength() {
    form.setCancellationReason(ValidatorTestUtils.exactlyMaxDefaultCharLength());
    var errors = ValidatorTestUtils.getFormValidationErrors(cancelAppChargeFormValidator, form);
    assertThat(errors).isEmpty();
  }
}