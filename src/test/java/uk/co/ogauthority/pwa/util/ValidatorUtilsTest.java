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
  public void validateDate_WithNulls() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDate("proposedStart", "proposed start", null, null, null, errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactlyInAnyOrder(
            "proposedStartDay.invalid",
            "proposedStartMonth.invalid",
            "proposedStartYear.invalid"
        );
    assertThat(validationNoErrors).isFalse();
  }

  @Test
  public void validateDate_ValidDate() {
    var date = LocalDate.now();
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDate("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(ObjectError::getObjectName)
        .doesNotContain("proposedStartDay", "proposedStartMonth", "proposedStartYear");
    assertThat(validationNoErrors).isTrue();
  }

  @Test
  public void validateDateIsPresentOrFuture_Past() {
    var date = LocalDate.now().minusDays(2);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDateIsPresentOrFuture("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactlyInAnyOrder(
            "proposedStartDay.beforeToday",
            "proposedStartMonth.beforeToday",
            "proposedStartYear.beforeToday"
        );
    assertThat(validationNoErrors).isFalse();
  }

  @Test
  public void validateDateIsPresentOrFuture_Future() {
    var date = LocalDate.now().plusDays(2);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDateIsPresentOrFuture("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(ObjectError::getObjectName)
        .doesNotContain("proposedStartDay", "proposedStartMonth", "proposedStartYear");
    assertThat(validationNoErrors).isTrue();
  }

  @Test
  public void validateDateIsPresentOrFuture_Present() {
    var date = LocalDate.now();
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDateIsPresentOrFuture("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(ObjectError::getObjectName)
        .doesNotContain("proposedStartDay", "proposedStartMonth", "proposedStartYear");
    assertThat(validationNoErrors).isTrue();
  }

  @Test
  public void validateDateIsPastOrPresent_Past() {
    var date = LocalDate.now().minusDays(2);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDateIsPastOrPresent("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(ObjectError::getObjectName)
        .doesNotContain("proposedStartDay", "proposedStartMonth", "proposedStartYear");
    assertThat(validationNoErrors).isTrue();
  }

  @Test
  public void validateDateIsPastOrPresent_Future() {
    var date = LocalDate.now().plusDays(2);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDateIsPastOrPresent("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactlyInAnyOrder(
            "proposedStartDay.afterToday",
            "proposedStartMonth.afterToday",
            "proposedStartYear.afterToday"
        );
    assertThat(validationNoErrors).isFalse();
  }

  @Test
  public void validateDateIsPastOrPresent_Present() {
    var date = LocalDate.now();
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    var validationNoErrors = ValidatorUtils.validateDateIsPastOrPresent("proposedStart", "proposed start",
        date.getDayOfMonth(), date.getMonthValue(), date.getYear(), errors);
    assertThat(errors.getAllErrors()).extracting(ObjectError::getObjectName)
        .doesNotContain("proposedStartDay", "proposedStartMonth", "proposedStartYear");
    assertThat(validationNoErrors).isTrue();
  }

  @Test
  public void validateBoolean_Null() {
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateBoolean(errors, projectInformationForm.getUsingCampaignApproach(), "usingCampaignApproach", "Err");
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .contains("usingCampaignApproach.required");
  }

  @Test
  public void validateBoolean_False() {
    projectInformationForm.setUsingCampaignApproach(false);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateBoolean(errors, projectInformationForm.getUsingCampaignApproach(),"usingCampaignApproach", "Err");
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .contains("usingCampaignApproach.required");
  }

  @Test
  public void validateBoolean_True() {
    projectInformationForm.setUsingCampaignApproach(true);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateBoolean(errors, projectInformationForm.getUsingCampaignApproach(),"usingCampaignApproach", "Err");
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .doesNotContain("usingCampaignApproach.required");
  }
}