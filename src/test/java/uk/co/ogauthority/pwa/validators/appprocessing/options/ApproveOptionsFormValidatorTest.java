package uk.co.ogauthority.pwa.validators.appprocessing.options;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.BEFORE_TODAY;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.appprocessing.options.ApproveOptionsForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;


@RunWith(MockitoJUnitRunner.class)
public class ApproveOptionsFormValidatorTest {

  private static final String DAY_ATTR = "deadlineDateDay";
  private static final String MONTH_ATTR = "deadlineDateMonth";
  private static final String YEAR_ATTR = "deadlineDateYear";

  private ApproveOptionsFormValidator approveOptionsFormValidator;

  private ApproveOptionsForm form;

  @Before
  public void setUp() throws Exception {
    approveOptionsFormValidator = new ApproveOptionsFormValidator();
    form = new ApproveOptionsForm();
  }

  @Test
  public void supports_whenValidTarget() {
    assertThat(approveOptionsFormValidator.supports(ApproveOptionsForm.class)).isTrue();
  }

  @Test
  public void supports_whenInvalidTarget() {
    assertThat(approveOptionsFormValidator.supports(Object.class)).isFalse();
  }

  @Test
  public void validate_whenAllNull() {

    var result = ValidatorTestUtils.getFormValidationErrors(approveOptionsFormValidator, form);

    assertThat(result).containsOnly(
        entry(DAY_ATTR, Set.of(REQUIRED.errorCode(DAY_ATTR))),
        entry(MONTH_ATTR, Set.of(REQUIRED.errorCode(MONTH_ATTR))),
        entry(YEAR_ATTR, Set.of(REQUIRED.errorCode(YEAR_ATTR)))
    );

  }

  @Test
  public void validate_whenPastDate() {
    form.setDeadlineDateDay(1);
    form.setDeadlineDateMonth(1);
    form.setDeadlineDateYear(2020);

    var result = ValidatorTestUtils.getFormValidationErrors(approveOptionsFormValidator, form);
    assertThat(result).containsOnly(
        entry(DAY_ATTR, Set.of(BEFORE_TODAY.errorCode(DAY_ATTR))),
        entry(MONTH_ATTR, Set.of(BEFORE_TODAY.errorCode(MONTH_ATTR))),
        entry(YEAR_ATTR, Set.of(BEFORE_TODAY.errorCode(YEAR_ATTR)))
    );

  }
}