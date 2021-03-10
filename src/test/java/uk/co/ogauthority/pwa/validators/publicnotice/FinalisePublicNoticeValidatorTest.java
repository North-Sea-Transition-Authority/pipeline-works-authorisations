package uk.co.ogauthority.pwa.validators.publicnotice;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class FinalisePublicNoticeValidatorTest {

  private FinalisePublicNoticeValidator validator;


  @Before
  public void setUp() {
    validator = new FinalisePublicNoticeValidator();
  }


  @Test
  public void validate_form_empty() {
    var form = new FinalisePublicNoticeForm();
    form.setDaysToBePublishedFor(null);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).containsOnly(
        entry("startDay", Set.of("startDay" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("startMonth", Set.of("startMonth" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("startYear", Set.of("startYear" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("daysToBePublishedFor", Set.of("daysToBePublishedFor" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_form_valid() {
    var form = new FinalisePublicNoticeForm();
    form.setStartDay(1);
    form.setStartMonth(1);
    form.setStartYear(2020);
    form.setDaysToBePublishedFor(28);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }


}