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
import uk.co.ogauthority.pwa.model.dto.consultations.ConsultationRequestDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConsultationResponseValidatorTest {

  private ConsultationResponseValidator validator;

  private ConsultationRequestDto contentConsultationRequestDto, adviceConsultationRequestDto;
  private ConsulteeGroupDetail contentConsulteeGroupDetail, adviceConsulteeGroupDetail;

  @Before
  public void setUp() {

    validator = new ConsultationResponseValidator();

    contentConsulteeGroupDetail = new ConsulteeGroupDetail();
    contentConsulteeGroupDetail.setResponseOptionGroups(Set.of(ConsultationResponseOptionGroup.CONTENT));
    contentConsultationRequestDto = new ConsultationRequestDto(contentConsulteeGroupDetail, null);

    adviceConsulteeGroupDetail = new ConsulteeGroupDetail();
    adviceConsulteeGroupDetail.setResponseOptionGroups(Set.of(ConsultationResponseOptionGroup.ADVICE));
    adviceConsultationRequestDto = new ConsultationRequestDto(adviceConsulteeGroupDetail, null);

  }

  @Test
  public void validate_form_empty() {
    var form = new ConsultationResponseForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        contentConsultationRequestDto);
    assertThat(errorsMap).containsOnly(
        entry("consultationResponseOption", Set.of("consultationResponseOption" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_form_valid() {
    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        contentConsultationRequestDto);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void form_rejected_invalid() {
    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.REJECTED);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        contentConsultationRequestDto);
    assertThat(errorsMap).containsOnly(
        entry("option2Description", Set.of("option2Description" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void form_confirmed_withConditions_valid() {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);
    form.setOption1Description("confirm");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, contentConsultationRequestDto);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  public void validate_rejectDescriptionTooBig_failed() {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.REJECTED);
    form.setOption2Description(ValidatorTestUtils.over4000Chars());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, contentConsultationRequestDto);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "option2Description",
                Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("option2Description")))
        );

  }

  @Test
  public void validate_confirmDescriptionTooBig_failed() {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);
    form.setOption1Description(ValidatorTestUtils.over4000Chars());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, contentConsultationRequestDto);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "option1Description",
                Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("option1Description")))
        );

  }

  @Test
  public void form_provideAdvice_withAdvice_valid() {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.PROVIDE_ADVICE);
    form.setOption1Description("advice");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, adviceConsultationRequestDto);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  public void form_provideAdvice_noAdvice_invalid() {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.PROVIDE_ADVICE);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, adviceConsultationRequestDto);
    assertThat(errorsMap).containsOnly(entry("option1Description", Set.of("option1Description" + FieldValidationErrorCodes.REQUIRED.getCode())));

  }

  @Test
  public void form_noAdvice_noComments_valid() {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.NO_ADVICE);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, adviceConsultationRequestDto);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  public void form_noAdvice_comments_valid() {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.NO_ADVICE);
    form.setOption2Description("comments");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, adviceConsultationRequestDto);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  public void validate_commentsTooBig_failed() {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.NO_ADVICE);
    form.setOption2Description(ValidatorTestUtils.over4000Chars());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, adviceConsultationRequestDto);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "option2Description",
                Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("option2Description")))
        );

  }

  @Test
  public void validate_adviceTextTooBig_failed() {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.PROVIDE_ADVICE);
    form.setOption1Description(ValidatorTestUtils.over4000Chars());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, contentConsultationRequestDto);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "option1Description",
                Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("option1Description")))
        );

  }

}