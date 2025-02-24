package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

class CancelAppChargeFormValidatorTest {
  private static final String CANCEL_REASON_ATTR = "cancellationReason";

  private CancelAppChargeFormValidator cancelAppChargeFormValidator;
  private CancelAppChargeForm form;

  @BeforeEach
  void setUp() throws Exception {
    cancelAppChargeFormValidator = new CancelAppChargeFormValidator();
    form = new CancelAppChargeForm();
  }

  @Test
  void supports_whenValid() {
    assertThat(cancelAppChargeFormValidator.supports(CancelAppChargeForm.class)).isTrue();
  }

  @Test
  void supports_whenInvalid() {
    assertThat(cancelAppChargeFormValidator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenCancelReasonNull() {
    var errors = ValidatorTestUtils.getFormValidationErrors(cancelAppChargeFormValidator, form);
    assertThat(errors).contains(
        entry(CANCEL_REASON_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(CANCEL_REASON_ATTR))));
  }

  @Test
  void validate_whenCancelReasonOverMaxDefaultCharLength() {
    form.setCancellationReason(ValidatorTestUtils.overMaxDefaultCharLength());
    var errors = ValidatorTestUtils.getFormValidationErrors(cancelAppChargeFormValidator, form);
    assertThat(errors).contains(
        entry(CANCEL_REASON_ATTR, Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode(CANCEL_REASON_ATTR))));
  }

  @Test
  void validate_whenCancelReasonExactlyMaxDefaultCharLength() {
    form.setCancellationReason(ValidatorTestUtils.exactlyMaxDefaultCharLength());
    var errors = ValidatorTestUtils.getFormValidationErrors(cancelAppChargeFormValidator, form);
    assertThat(errors).isEmpty();
  }
}