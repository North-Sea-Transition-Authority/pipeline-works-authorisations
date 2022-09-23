package uk.co.ogauthority.pwa.features.feemanagement.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriod;
import uk.co.ogauthority.pwa.model.form.feeperiod.FeePeriodForm;

@RunWith(MockitoJUnitRunner.class)
public class FeePeriodValidatorTest {

  private FeePeriodValidator validator;

  private Errors errorList;

  private FeePeriodForm form;

  private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyy")
      .withZone(ZoneId.systemDefault());

  @Before
  public void setup() {
    form = new FeePeriodForm();
    form.setPeriodDescription("Test Description");
    form.setPeriodStartDate(formatter.format(Instant.now()));
    form.setApplicationCostMap(new HashMap<>());

    validator = new FeePeriodValidator();
    errorList = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  public void validateDescription_passOnFilled() {
    validator.validate(form, errorList);
    assertThat(getAllErrorCodes()).doesNotContain("newPeriod.Description");
  }

  @Test
  public void validateDescription_failOnNullOrEmpty() {
    form.setPeriodDescription("");
    validator.validate(form, errorList);
    assertThat(getAllErrorCodes()).contains("newPeriod.Description");

    form.setPeriodDescription(null);
    validator.validate(form, errorList);
    assertThat(getAllErrorCodes()).contains("newPeriod.Description");
  }

  @Test
  public void validateStartDate_passOnFilled() {
    validator.validate(form, errorList);
    assertThat(getAllErrorCodes()).doesNotContain("newPeriod.startDate.empty");
    assertThat(getAllErrorCodes()).doesNotContain("newPeriod.startDate.invalid");
  }

  @Test
  public void validateStartDate_failOnEmpty() {
    form.setPeriodStartDate(null);
    validator.validate(form, errorList);
    assertThat(getAllErrorCodes()).contains("periodStartDate.required");
  }

  @Test
  public void validateStartDate_failOnInPast() {
    var date = Instant.now().minus(10, ChronoUnit.DAYS);
    form.setPeriodStartDate(formatter.format(date));

    validator.validate(form, errorList);
    assertThat(getAllErrorCodes()).contains("periodStartDate.beforeToday");
  }

  @Test
  public void validateStartDate_failOnInvalid() {
    form.setPeriodStartDate("I just want a new pipeline");

    validator.validate(form, errorList);
    assertThat(getAllErrorCodes()).contains("periodStartDate.invalid");
  }

  @Test
  public void validateCostMap_failOnNotCurrency() {
    var costMap = new HashMap<String, String>();
    costMap.put(PwaApplicationType.INITIAL.name() + ":" + PwaApplicationFeeType.DEFAULT.name(), "500 pounds");
    form.setApplicationCostMap(costMap);

    validator.validate(form, errorList);
    assertThat(getAllErrorCodes()).contains("newPeriod.feeValue.numberFormat");
  }

  @Test
  public void validateCostMap_failOnNegative() {
    var costMap = new HashMap<String, String>();
    costMap.put(PwaApplicationType.INITIAL.name() + ":" + PwaApplicationFeeType.DEFAULT.name(), "-500.00");
    form.setApplicationCostMap(costMap);

    validator.validate(form, errorList);
    assertThat(getAllErrorCodes()).contains("newPeriod.feeValue.invalid");
  }

  @Test
  public void validateCostMap_failOnDecimalPlaces() {
    var costMap = new HashMap<String, String>();
    costMap.put(PwaApplicationType.INITIAL.name() + ":" + PwaApplicationFeeType.DEFAULT.name(), "500.111111");
    form.setApplicationCostMap(costMap);

    validator.validate(form, errorList);
    assertThat(getAllErrorCodes()).contains("newPeriod.feeValue.invalid");
  }

  @Test
  public void validateCostMap_validatesTrue() {
    var costMap = new HashMap<String, String>();
    costMap.put(PwaApplicationType.INITIAL.name() + ":" + PwaApplicationFeeType.DEFAULT.name(), "500.11");
    form.setApplicationCostMap(costMap);

    validator.validate(form, errorList);
    assertThat(getAllErrorCodes()).doesNotContain("newPeriod.feeValue.invalid");
    assertThat(getAllErrorCodes()).doesNotContain("newPeriod.feeValue.numberFormat");
  }

  @Test
  public void supportsPeriodForm_supportsForm() {
    var supports = validator.supports(FeePeriodForm.class);
    assertThat(supports).isTrue();
  }

  @Test
  public void supportsPeriodForm_notSupportOthers() {
    var supports = validator.supports(FeePeriod.class);
    assertThat(supports).isFalse();
  }

  private List<String> getAllErrorCodes() {
    return errorList.getAllErrors()
        .stream()
        .map(DefaultMessageSourceResolvable::getCode)
        .collect(Collectors.toList());
  }
}
