package uk.co.ogauthority.pwa.validators.consultations;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConsultationResponseValidatorTest {


  private ConsultationResponseValidator validator;

  @Before
  public void setUp() {
    validator = new ConsultationResponseValidator();
  }



  @Test
  public void validate_form_empty() {
    var form = new ConsultationResponseForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).containsOnly(
        entry("consultationResponseOption", Set.of("consultationResponseOption" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_form_valid() {
    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void form_rejected_invalid() {
    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.REJECTED);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).containsOnly(
        entry("rejectedDescription", Set.of("rejectedDescription" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void form_confirmed_withConditions_valid() {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);
    form.setConfirmedDescription("confirm");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  public void validate_rejectDescriptionTooBig_failed() {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.REJECTED);
    form.setRejectedDescription(ValidatorTestUtils.over4000Chars());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "rejectedDescription",
                Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("rejectedDescription")))
        );

  }

  @Test
  public void validate_confirmDescriptionTooBig_failed() {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);
    form.setConfirmedDescription(ValidatorTestUtils.over4000Chars());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "confirmedDescription",
                Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("confirmedDescription")))
        );

  }

}