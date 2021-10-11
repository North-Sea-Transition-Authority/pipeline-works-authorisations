package uk.co.ogauthority.pwa.validators.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.feedback.FeedbackService;
import uk.co.ogauthority.pwa.service.feedback.FeedbackTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;


@RunWith(MockitoJUnitRunner.class)
public class FeedbackServiceValidationTest {


  private FeedbackValidator validator;

  @Before
  public void setup() {
    validator = new FeedbackValidator();
  }

  @Test
  public void validate_whenNoServiceRating_thenValidationErrorExpected() {

    var form = FeedbackTestUtil.getValidFeedbackForm();
    form.setServiceRating(null);

    var fieldErrors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(fieldErrors).containsExactly(
        entry("serviceRating", Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("serviceRating")))
    );
  }

  @Test
  public void validate_whenNoFeedback_thenNoValidationErrorExpected() {

    var form = FeedbackTestUtil.getValidFeedbackForm();
    form.setFeedback(null);

    var fieldErrors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(fieldErrors).isEmpty();
  }

  @Test
  public void validate_whenFeedbackExceedsCharacterLimit_thenValidationErrorExpected() {

    var feedbackOverLimit = ValidatorTestUtils.overCharLength(
        FeedbackService.FEEDBACK_CHARACTER_LIMIT + 1
    );

    var form = FeedbackTestUtil.getValidFeedbackForm();
    form.setFeedback(feedbackOverLimit);

    var fieldErrors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(fieldErrors).containsExactly(
        entry("feedback", Set.of("feedback" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }

  @Test
  public void validate_whenValidFeedbackForm_thenNoValidationErrorExpected() {
    var form = FeedbackTestUtil.getValidFeedbackForm();

    var fieldErrors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(fieldErrors).isEmpty();
  }
}
