package uk.co.ogauthority.pwa.validators.consultations;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseDataForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class ConsultationResponseDataValidatorTest {

  private ConsultationResponseDataValidator validator;

  @BeforeEach
  void setUp() {

    validator = new ConsultationResponseDataValidator();

  }

  @Test
  void validate_form_empty() {
    var form = new ConsultationResponseDataForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        ConsultationResponseOptionGroup.CONTENT);
    assertThat(errorsMap).containsOnly(
        entry("consultationResponseOption", Set.of("consultationResponseOption" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_form_valid() {
    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        ConsultationResponseOptionGroup.CONTENT);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void form_rejected_invalid() {
    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.REJECTED);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        ConsultationResponseOptionGroup.CONTENT);
    assertThat(errorsMap).containsOnly(
        entry("option2Description", Set.of("option2Description" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void form_confirmed_withConditions_valid() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);
    form.setOption1Description("confirm");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.CONTENT);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_rejectDescriptionTooBig_failed() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.REJECTED);
    form.setOption2Description(ValidatorTestUtils.overMaxDefaultCharLength());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.CONTENT);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "option2Description",
                Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("option2Description")))
        );

  }

  @Test
  void validate_confirmDescriptionTooBig_failed() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);
    form.setOption1Description(ValidatorTestUtils.overMaxDefaultCharLength());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.CONTENT);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "option1Description",
                Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("option1Description")))
        );

  }

  @Test
  void form_provideAdvice_withAdvice_valid() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.PROVIDE_ADVICE);
    form.setOption1Description("advice");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.ADVICE);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  void form_provideAdvice_noAdvice_invalid() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.PROVIDE_ADVICE);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.ADVICE);
    assertThat(errorsMap).containsOnly(entry("option1Description", Set.of("option1Description" + FieldValidationErrorCodes.REQUIRED.getCode())));

  }

  @Test
  void form_noAdvice_noComments_valid() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.NO_ADVICE);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.ADVICE);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  void form_noAdvice_comments_valid() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.NO_ADVICE);
    form.setOption2Description("comments");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.ADVICE);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_commentsTooBig_failed() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.NO_ADVICE);
    form.setOption2Description(ValidatorTestUtils.overMaxDefaultCharLength());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.ADVICE);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "option2Description",
                Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("option2Description")))
        );

  }

  @Test
  void validate_adviceTextTooBig_failed() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.PROVIDE_ADVICE);
    form.setOption1Description(ValidatorTestUtils.overMaxDefaultCharLength());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.ADVICE);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "option1Description",
                Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("option1Description")))
        );

  }

  @Test
  void validate_eiaNotRelevantTextTooBig_failed() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.EIA_NOT_RELEVANT);
    form.setOption3Description(ValidatorTestUtils.overMaxDefaultCharLength());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "option3Description",
                Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("option3Description")))
        );

  }

  @Test
  void validate_habitatsNotRelevantTextTooBig_failed() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.HABITATS_NOT_RELEVANT);
    form.setOption3Description(ValidatorTestUtils.overMaxDefaultCharLength());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.HABITATS_REGS);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "option3Description",
                Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("option3Description")))
        );

  }

  @Test
  void validate_eia_agree_nothingProvided_pass() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.EIA_AGREE);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_eia_agree_conditionsProvided_pass() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.EIA_AGREE);
    form.setOption1Description("conditions");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_eia_disagree_nothingProvided_fail() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.EIA_DISAGREE);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap).containsOnly(entry("option2Description", Set.of("option2Description" + FieldValidationErrorCodes.REQUIRED.getCode())));

  }

  @Test
  void validate_eia_disagree_commentsProvided_pass() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.EIA_DISAGREE);
    form.setOption2Description("comments");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_eia_notRelevant_nothingProvided_fail() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.EIA_NOT_RELEVANT);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap).containsOnly(entry("option3Description", Set.of("option3Description" + FieldValidationErrorCodes.REQUIRED.getCode())));

  }

  @Test
  void validate_eia_notRelevant_commentsProvided_pass() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.EIA_NOT_RELEVANT);
    form.setOption3Description("comments");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_habitats_agree_nothingProvided_pass() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.HABITATS_AGREE);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.HABITATS_REGS);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_habitats_agree_conditionsProvided_pass() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.HABITATS_AGREE);
    form.setOption1Description("conditions");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.HABITATS_REGS);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_habitats_disagree_nothingProvided_fail() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.HABITATS_DISAGREE);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.HABITATS_REGS);
    assertThat(errorsMap).containsOnly(entry("option2Description", Set.of("option2Description" + FieldValidationErrorCodes.REQUIRED.getCode())));

  }

  @Test
  void validate_habitats_disagree_commentsProvided_pass() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.HABITATS_DISAGREE);
    form.setOption2Description("comments");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.HABITATS_REGS);
    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_habitats_notRelevant_nothingProvided_fail() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.HABITATS_NOT_RELEVANT);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.HABITATS_REGS);
    assertThat(errorsMap).containsOnly(entry("option3Description", Set.of("option3Description" + FieldValidationErrorCodes.REQUIRED.getCode())));

  }

  @Test
  void validate_habitats_notRelevant_commentsProvided_pass() {

    var form = new ConsultationResponseDataForm();
    form.setConsultationResponseOption(ConsultationResponseOption.HABITATS_NOT_RELEVANT);
    form.setOption3Description("comments");

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.HABITATS_REGS);
    assertThat(errorsMap).isEmpty();

  }

}