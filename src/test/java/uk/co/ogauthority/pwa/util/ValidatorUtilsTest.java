package uk.co.ogauthority.pwa.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;

public class ValidatorUtilsTest {

  private ProjectInformationForm projectInformationForm;

  @Before
  public void setUp() {
    projectInformationForm = new ProjectInformationForm();
  }

  @Test
  public void isIntegerBetween_Null() {
    assertThat(ValidatorUtils.isIntegerBetween(null, 1, 10)).isFalse();
  }

  @Test
  public void isIntegerBetween_LessThan() {
    assertThat(ValidatorUtils.isIntegerBetween(0, 1, 10)).isFalse();
  }

  @Test
  public void isIntegerBetween_GreaterThan() {
    assertThat(ValidatorUtils.isIntegerBetween(11, 1, 10)).isFalse();
  }

  @Test
  public void isIntegerBetween_InBounds() {
    assertThat(ValidatorUtils.isIntegerBetween(1, 1, 10)).isTrue();
    assertThat(ValidatorUtils.isIntegerBetween(5, 1, 10)).isTrue();
    assertThat(ValidatorUtils.isIntegerBetween(10, 1, 10)).isTrue();
  }

  @Test
  public void validateDate_WithNulls() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateDate("proposedStart", "proposed start", null, null, null, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactlyInAnyOrder(
            "proposedStartDay.invalid",
            "proposedStartMonth.invalid",
            "proposedStartYear.invalid"
        );
  }

  @Test
  public void validateDate_InPast() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateDate("proposedStart", "proposed start", 1, 1, 2020, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactlyInAnyOrder(
            "proposedStartDay.beforeToday",
            "proposedStartMonth.beforeToday",
            "proposedStartYear.beforeToday"
        );
  }

  @Test
  public void validateDate_InFuture() {
    var date = LocalDate.now().plusDays(2);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateDate("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(ObjectError::getObjectName)
        .doesNotContain("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }
}