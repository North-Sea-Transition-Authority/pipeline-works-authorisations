package uk.co.ogauthority.pwa.util;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

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
    ValidatorUtils.validateBooleanTrue(errors, projectInformationForm.getUsingCampaignApproach(), "usingCampaignApproach", "Err");
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .contains("usingCampaignApproach.required");
  }

  @Test
  public void validateBoolean_False() {
    projectInformationForm.setUsingCampaignApproach(false);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateBooleanTrue(errors, projectInformationForm.getUsingCampaignApproach(),"usingCampaignApproach", "Err");
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .contains("usingCampaignApproach.required");
  }

  @Test
  public void validateBoolean_True() {
    projectInformationForm.setUsingCampaignApproach(true);
    Errors errors = new BeanPropertyBindingResult(projectInformationForm, "form");
    ValidatorUtils.validateBooleanTrue(errors, projectInformationForm.getUsingCampaignApproach(),"usingCampaignApproach", "Err");
    assertThat(errors.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .doesNotContain("usingCampaignApproach.required");
  }

  @Test
  public void validateLatitude_valid() {

    var bindingResult = new BeanPropertyBindingResult(new CoordinateForm(), "form");
    ValidatorUtils.validateLatitude(
        bindingResult,
        "Start point",
        Pair.of("latitudeDegrees", 55),
        Pair.of("latitudeMinutes", 30),
        Pair.of("latitudeSeconds", BigDecimal.valueOf(44.44)));

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validateLatitude_invalid() {

    var bindingResult = new BeanPropertyBindingResult(new CoordinateForm(), "form");
    ValidatorUtils.validateLatitude(
        bindingResult,
        "Start point",
        Pair.of("latitudeDegrees", 44),
        Pair.of("latitudeMinutes", -1),
        Pair.of("latitudeSeconds", BigDecimal.valueOf(61)));

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("latitudeDegrees", Set.of("latitudeDegrees.invalid")),
        entry("latitudeMinutes", Set.of("latitudeMinutes.invalid")),
        entry("latitudeSeconds", Set.of("latitudeSeconds.invalid"))
    );

  }

  @Test
  public void validateLatitude_invalidSecondPrecision() {

    var bindingResult = new BeanPropertyBindingResult(new CoordinateForm(), "form");
    ValidatorUtils.validateLatitude(
        bindingResult,
        "Start point",
        Pair.of("latitudeDegrees", 55),
        Pair.of("latitideMinutes", 30),
        Pair.of("latitudeSeconds", BigDecimal.valueOf(45.555)));

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("latitudeSeconds", Set.of("latitudeSeconds.invalid"))
    );

  }

  @Test
  public void validateLongitude_valid() {

    var bindingResult = new BeanPropertyBindingResult(new CoordinateForm(), "form");
    ValidatorUtils.validateLongitude(
        bindingResult,
        "Start point",
        Pair.of("longitudeDegrees", 29),
        Pair.of("longitudeMinutes", 30),
        Pair.of("longitudeSeconds", BigDecimal.valueOf(44.44)),
        Pair.of("longitudeDirection", LongitudeDirection.EAST));

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validateLongitude_invalid() {

    var bindingResult = new BeanPropertyBindingResult(new CoordinateForm(), "form");
    ValidatorUtils.validateLongitude(
        bindingResult,
        "Start point",
        Pair.of("longitudeDegrees", 31),
        Pair.of("longitudeMinutes", 61),
        Pair.of("longitudeSeconds", BigDecimal.valueOf(-1)),
        Pair.of("longitudeDirection", LongitudeDirection.EAST));

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("longitudeDegrees", Set.of("longitudeDegrees.invalid")),
        entry("longitudeMinutes", Set.of("longitudeMinutes.invalid")),
        entry("longitudeSeconds", Set.of("longitudeSeconds.invalid"))
    );

  }

  @Test
  public void validateLongitude_invalidSecondPrecision() {

    var bindingResult = new BeanPropertyBindingResult(new CoordinateForm(), "form");
    ValidatorUtils.validateLongitude(
        bindingResult,
        "Start point",
        Pair.of("longitudeDegrees", 22),
        Pair.of("longitudeMinutes", 30),
        Pair.of("longitudeSeconds", BigDecimal.valueOf(33.333)),
        Pair.of("longitudeDirection", LongitudeDirection.WEST));

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("longitudeSeconds", Set.of("longitudeSeconds.invalid"))
    );

  }

}