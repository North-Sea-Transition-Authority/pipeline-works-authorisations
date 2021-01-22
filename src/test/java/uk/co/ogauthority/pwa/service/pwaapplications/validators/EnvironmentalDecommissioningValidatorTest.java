package uk.co.ogauthority.pwa.service.pwaapplications.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.EnvironmentalDecommissioningForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.EnvironmentalDecommissioningValidator;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentalDecommissioningValidatorTest {

  @Mock
  private PadEnvironmentalDecommissioningService environmentalDecommissioningService;

  private EnvironmentalDecommissioningValidator validator;

  private PwaApplicationDetail initialAppDetail;
  private PwaApplicationDetail cat2AppDetail;

  List<PwaApplicationDetail> details;

  @Before
  public void setUp() {

    validator = new EnvironmentalDecommissioningValidator(environmentalDecommissioningService);

    initialAppDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    cat2AppDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_2_VARIATION);

    when(environmentalDecommissioningService.getAvailableQuestions(any())).thenCallRealMethod();

    details = List.of(initialAppDetail, cat2AppDetail);

  }

  @Test
  public void initial_mandatoryDataNotProvided_failed() {

    var form = new EnvironmentalDecommissioningForm();
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, initialAppDetail, ValidationType.FULL);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactlyInAnyOrder(
            tuple("transboundaryEffect", Set.of("transboundaryEffect.required")),
            tuple("emtHasSubmittedPermits", Set.of("emtHasSubmittedPermits.required")),
            tuple("emtHasOutstandingPermits", Set.of("emtHasOutstandingPermits.required")),
            tuple("environmentalConditions", Set.of("environmentalConditions.requiresAll")),
            tuple("decommissioningConditions", Set.of("decommissioningConditions.requiresAll"))
        );

  }

  @Test
  public void cat2_mandatoryDataNotProvided_failed() {

    var form = new EnvironmentalDecommissioningForm();
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, cat2AppDetail, ValidationType.FULL);

    assertThat(errors)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactlyInAnyOrder(
            tuple("emtHasSubmittedPermits", Set.of("emtHasSubmittedPermits.required")),
            tuple("emtHasOutstandingPermits", Set.of("emtHasOutstandingPermits.required"))
        );

  }

  @Test
  public void initial_mandatoryDataProvided_passed() {

    var form = buildForm();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, initialAppDetail, ValidationType.FULL);

    assertThat(errors).isEmpty();

  }

  @Test
  public void cat2_mandatoryDataProvided_passed() {

    var form = new EnvironmentalDecommissioningForm();
    form.setEmtHasSubmittedPermits(true);
    form.setPermitsSubmitted("submitted");
    form.setEmtHasOutstandingPermits(false);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, cat2AppDetail, ValidationType.FULL);

    assertThat(errors).isEmpty();

  }

  @Test
  public void all_partial_noFieldsMandatory() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();

      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.PARTIAL);

      assertThat(errors).isEmpty();

    });

  }

  @Test
  public void all_partial_someDataEntered_newlyVisibleFieldsNotMand() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasOutstandingPermits(true);
      form.setEmtHasSubmittedPermits(true);

      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.PARTIAL);

      assertThat(errors).isEmpty();

    });

  }

  @Test
  public void all_partial_datePartiallyProvided_validatedFully() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasOutstandingPermits(true);
      form.setEmtSubmissionDay(23);

      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.PARTIAL);

      assertThat(errors.get("emtSubmissionDay")).containsExactly("emtSubmissionDay.notParsable");
      assertThat(errors.get("emtSubmissionMonth")).containsExactly("emtSubmissionMonth.notParsable");
      assertThat(errors.get("emtSubmissionYear")).containsExactly("emtSubmissionYear.notParsable");

    });

  }

  @Test
  public void testValidate_EmptyPermitsSubmittedField_full() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasSubmittedPermits(true);
      form.setPermitsSubmitted("");
      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.FULL);
      assertThat(errors.get("permitsSubmitted")).containsExactly("permitsSubmitted.required");

    });

  }

  @Test
  public void testValidate_ValidPermitsSubmittedField_full() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasSubmittedPermits(true);
      form.setPermitsSubmitted("Test");
      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.FULL);
      assertThat(errors.get("permitsSubmitted")).isNull();

    });

  }

  @Test
  public void testValidate_permitsSubmitted_maxLengthExceeded() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasSubmittedPermits(true);
      form.setPermitsSubmitted(ValidatorTestUtils.over4000Chars());

      Arrays.stream(ValidationType.values()).forEach(validationType -> {

        var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, validationType);
        assertThat(errors.get("permitsSubmitted")).containsExactly("permitsSubmitted.maxLengthExceeded");

      });

    });

  }

  @Test
  public void testValidate_bothPermitQuestionsAnsweredNo() {
    var form = new EnvironmentalDecommissioningForm();
    form.setEmtHasSubmittedPermits(false);
    form.setEmtHasOutstandingPermits(false);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, initialAppDetail, ValidationType.FULL);
    assertThat(errors.get("emtHasSubmittedPermits")).containsExactly("emtHasSubmittedPermits" + FieldValidationErrorCodes.INVALID.getCode());
    assertThat(errors.get("emtHasOutstandingPermits")).containsExactly("emtHasOutstandingPermits" + FieldValidationErrorCodes.INVALID.getCode());
  }

  @Test
  public void testValidate_hasSubmittedPermitQuestionAnsweredYes() {
    var form = new EnvironmentalDecommissioningForm();
    form.setEmtHasSubmittedPermits(true);
    form.setEmtHasOutstandingPermits(false);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, initialAppDetail, ValidationType.FULL);
    assertThat(errorsMap).doesNotContain(
        entry("emtHasSubmittedPermits", Set.of("emtHasSubmittedPermits" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("emtHasOutstandingPermits", Set.of("emtHasOutstandingPermits" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  public void testValidate_hasOutstandingPermitQuestionAnsweredYes() {
    var form = new EnvironmentalDecommissioningForm();
    form.setEmtHasSubmittedPermits(false);
    form.setEmtHasOutstandingPermits(true);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, initialAppDetail, ValidationType.FULL);
    assertThat(errorsMap).doesNotContain(
        entry("emtHasSubmittedPermits", Set.of("emtHasSubmittedPermits" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("emtHasOutstandingPermits", Set.of("emtHasOutstandingPermits" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  public void testValidate_NullPermitsPendingSubmissionField_full() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasOutstandingPermits(true);
      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.FULL);
      assertThat(errors.get("permitsPendingSubmission")).containsExactly("permitsPendingSubmission.required");

    });

  }

  @Test
  public void testValidate_EmptyPermitsPendingSubmissionField_full() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasOutstandingPermits(true);
      form.setPermitsPendingSubmission("");
      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.FULL);
      assertThat(errors.get("permitsPendingSubmission")).containsExactly("permitsPendingSubmission.required");

    });

  }

  @Test
  public void testValidate_ValidPermitsPendingSubmissionField_full() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasOutstandingPermits(true);
      form.setPermitsPendingSubmission("Test");
      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.FULL);
      assertThat(errors.get("permitsPendingSubmission")).isNull();

    });

  }

  @Test
  public void testValidate_permitsPendingSubmission_maxLengthExceeded() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasOutstandingPermits(true);
      form.setPermitsPendingSubmission(ValidatorTestUtils.over4000Chars());

      Arrays.stream(ValidationType.values()).forEach(validationType -> {

        var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, validationType);
        assertThat(errors.get("permitsPendingSubmission")).containsExactly("permitsPendingSubmission.maxLengthExceeded");

      });

    });

  }

  @Test
  public void testValidate_EmptyDate_full() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasOutstandingPermits(true);
      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.FULL);
      assertThat(errors.get("emtSubmissionDay")).containsExactly("emtSubmissionDay.notParsable");
      assertThat(errors.get("emtSubmissionMonth")).containsExactly("emtSubmissionMonth.notParsable");
      assertThat(errors.get("emtSubmissionYear")).containsExactly("emtSubmissionYear.notParsable");

    });

  }

  @Test
  public void testValidate_InvalidDate_full() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasOutstandingPermits(true);
      form.setEmtSubmissionDay(-1);
      form.setEmtSubmissionMonth(-1);
      form.setEmtSubmissionYear(-1);
      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.FULL);
      assertThat(errors.get("emtSubmissionDay")).containsExactly("emtSubmissionDay.invalidDate", "emtSubmissionDay.invalid");
      assertThat(errors.get("emtSubmissionMonth")).containsExactly("emtSubmissionMonth.invalidDate", "emtSubmissionMonth.invalid");
      assertThat(errors.get("emtSubmissionYear")).containsExactly("emtSubmissionYear.invalid", "emtSubmissionYear.invalidDate");

    });

  }

  @Test
  public void testValidate_yearTooBig() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasOutstandingPermits(true);
      form.setEmtSubmissionYear(4001);
      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.PARTIAL);
      assertThat(errors.get("emtSubmissionYear")).contains("emtSubmissionYear" + FieldValidationErrorCodes.INVALID.getCode());
    });
  }

  @Test
  public void testValidate_yearTooSmall() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasOutstandingPermits(true);
      form.setEmtSubmissionYear(999);
      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.PARTIAL);
      assertThat(errors.get("emtSubmissionYear")).contains("emtSubmissionYear" + FieldValidationErrorCodes.INVALID.getCode());
    });
  }

  @Test
  public void testValidate_ValidDate_full() {

    details.forEach(detail -> {

      var form = new EnvironmentalDecommissioningForm();
      form.setEmtHasOutstandingPermits(true);
      form.setEmtSubmissionDay(16);
      form.setEmtSubmissionMonth(3);
      form.setEmtSubmissionYear(2020);
      var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, ValidationType.FULL);
      assertThat(errors.get("emtSubmissionDay")).isNull();
      assertThat(errors.get("emtSubmissionMonth")).isNull();
      assertThat(errors.get("emtSubmissionYear")).isNull();

    });

  }

  private EnvironmentalDecommissioningForm buildForm() {

    var form = new EnvironmentalDecommissioningForm();
    form.setTransboundaryEffect(true);
    form.setEmtHasSubmittedPermits(true);
    form.setPermitsSubmitted("submitted");
    form.setEmtHasOutstandingPermits(false);
    form.setEnvironmentalConditions(EnumSet.allOf(EnvironmentalCondition.class));
    form.setDecommissioningConditions(EnumSet.allOf(DecommissioningCondition.class));

    return form;

  }

}