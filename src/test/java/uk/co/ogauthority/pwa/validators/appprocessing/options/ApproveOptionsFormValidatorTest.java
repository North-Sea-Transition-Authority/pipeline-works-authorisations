package uk.co.ogauthority.pwa.validators.appprocessing.options;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.BEFORE_SOME_DATE;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.form.appprocessing.options.ApproveOptionsForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;


@ExtendWith(MockitoExtension.class)
class ApproveOptionsFormValidatorTest {

  private static final String DAY_ATTR = "deadlineDateDay";
  private static final String MONTH_ATTR = "deadlineDateMonth";
  private static final String YEAR_ATTR = "deadlineDateYear";

  private ApproveOptionsFormValidator approveOptionsFormValidator;

  private ApproveOptionsForm form;

  @BeforeEach
  void setUp() throws Exception {
    approveOptionsFormValidator = new ApproveOptionsFormValidator();
    form = new ApproveOptionsForm();
  }

  @Test
  void supports_whenValidTarget() {
    assertThat(approveOptionsFormValidator.supports(ApproveOptionsForm.class)).isTrue();
  }

  @Test
  void supports_whenInvalidTarget() {
    assertThat(approveOptionsFormValidator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenAllNull() {

    var result = ValidatorTestUtils.getFormValidationErrors(approveOptionsFormValidator, form);

    assertThat(result).containsOnly(
        entry(DAY_ATTR, Set.of(REQUIRED.errorCode(DAY_ATTR))),
        entry(MONTH_ATTR, Set.of(REQUIRED.errorCode(MONTH_ATTR))),
        entry(YEAR_ATTR, Set.of(REQUIRED.errorCode(YEAR_ATTR)))
    );

  }

  @Test
  void validate_whenPastDate() {
    form.setDeadlineDateDay(1);
    form.setDeadlineDateMonth(1);
    form.setDeadlineDateYear(2020);

    var result = ValidatorTestUtils.getFormValidationErrors(approveOptionsFormValidator, form);
    assertThat(result).containsOnly(
        entry(DAY_ATTR, Set.of(BEFORE_SOME_DATE.errorCode(DAY_ATTR))),
        entry(MONTH_ATTR, Set.of(BEFORE_SOME_DATE.errorCode(MONTH_ATTR))),
        entry(YEAR_ATTR, Set.of(BEFORE_SOME_DATE.errorCode(YEAR_ATTR)))
    );

  }
}