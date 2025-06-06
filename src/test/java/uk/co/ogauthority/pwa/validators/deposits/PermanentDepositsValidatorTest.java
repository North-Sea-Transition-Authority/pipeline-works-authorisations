package uk.co.ogauthority.pwa.validators.deposits;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.MaterialType;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadPermanentDepositTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositsForm;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositsValidationHints;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositsValidator;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinateUtils;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeDirection;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInputValidator;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@ExtendWith(MockitoExtension.class)
public class PermanentDepositsValidatorTest {

  private final String CONCRETE_MATTRESS_LENGTH_ATTR = "concreteMattressLength";
  private final String CONCRETE_MATTRESS_WIDTH_ATTR = "concreteMattressWidth";
  private final String CONCRETE_MATTRESS_DEPTH_ATTR = "concreteMattressDepth";

  private final Set<String> ACCEPTED_PIPELINE_IDS = Set.of("1", "2");

  private final static Instant TODAY_TS = Instant.now();
  private final static LocalDate TODAY = DateUtils.instantToLocalDate(TODAY_TS);

  private PermanentDepositsValidator validator;

  private PermanentDepositsValidationHints validationHints;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {
    validator = new PermanentDepositsValidator(new TwoFieldDateInputValidator(),
        new CoordinateFormValidator(),
        new DecimalInputValidator()
    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    validationHints = PadPermanentDepositTestUtil.createValidationHints(pwaApplicationDetail, ACCEPTED_PIPELINE_IDS);
  }

  public PermanentDepositsForm getPermanentDepositsFormWithCoordinates() {
    var form = new PermanentDepositsForm();
    form.setFromDate(new TwoFieldDateInput());
    form.setToDate(new TwoFieldDateInput());
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    return form;
  }


  @Test
  void validate_consentedPipelinesAndOtherAppQuestionsBothAnsweredNo() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositIsForConsentedPipeline(false);
    form.setDepositIsForPipelinesOnOtherApp(false);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("depositIsForConsentedPipeline",
            Set.of("depositIsForConsentedPipeline" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("depositIsForPipelinesOnOtherApp",
            Set.of("depositIsForPipelinesOnOtherApp" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  void validate_consentedPipelinesNull() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("depositIsForConsentedPipeline",
        Set.of("depositIsForConsentedPipeline" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_otherAppQuestionNull() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("depositIsForPipelinesOnOtherApp",
        Set.of("depositIsForPipelinesOnOtherApp" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_consentedPipelinesAnsweredYes_pipelinesNotProvided() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositIsForConsentedPipeline(true);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("selectedPipelines", Set.of("selectedPipelines" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_consentedPipelinesAnsweredYes_invalidPipelineIdSelected() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositIsForConsentedPipeline(true);
    form.setSelectedPipelines(Set.of("Invalid pipeline id"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
      entry("selectedPipelines", Set.of("selectedPipelines" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  void validate_consentedPipelinesAnsweredYes_validPipelineIdSelected() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositIsForConsentedPipeline(true);
    form.setSelectedPipelines(ACCEPTED_PIPELINE_IDS);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(
      entry("selectedPipelines", Set.of("selectedPipelines" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  void validate_otherAppQuestionAnsweredYes_appRefAndNumNotProvided() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositIsForPipelinesOnOtherApp(true);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("appRefAndPipelineNum", Set.of("appRefAndPipelineNum" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_otherAppQuestionAnsweredYes_appRefAndNumNotProvided_lengthExceeded() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositIsForPipelinesOnOtherApp(true);
    form.setAppRefAndPipelineNum(ValidatorTestUtils.overMaxDefaultCharLength());
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("appRefAndPipelineNum",
            Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("appRefAndPipelineNum"))));
  }

  @Test
  void supports_whenSupported() {
    assertThat(validator.supports(PermanentDepositsForm.class)).isTrue();
  }

  @Test
  void supports_whenNotSupported() {
    assertThat(validator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_reference_blank() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("depositReference", Set.of("depositReference" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_reference_tooLong_invalid() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositReference(StringUtils.repeat('d', 51));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("depositReference",
        Set.of("depositReference" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

  @Test
  void validate_reference_maxAllowableLength_valid() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositReference(StringUtils.repeat('d', 50));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContainKeys("depositReference");
  }

  @Test
  void validate_reference_notUnique_invalid() {
    var form = PadPermanentDepositTestUtil.createDepositFormWithReference(1, "myRef");
    var depositSameRef = PadPermanentDepositTestUtil.createDepositsWithReference(2, "myRef");

    validationHints = PadPermanentDepositTestUtil.createValidationHintsWithDeposits(pwaApplicationDetail,
        List.of(depositSameRef));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("depositReference", Set.of(FieldValidationErrorCodes.NOT_UNIQUE.errorCode("depositReference"))));
  }

  @Test
  void validate_reference_notUnique_sameDeposit_valid() {
    var form = PadPermanentDepositTestUtil.createDepositFormWithReference(1, "myRef");
    var depositSameRef = PadPermanentDepositTestUtil.createDepositsWithReference(1, "myRef");

    validationHints = PadPermanentDepositTestUtil.createValidationHintsWithDeposits(pwaApplicationDetail,
        List.of(depositSameRef));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(
        entry("depositReference", Set.of(FieldValidationErrorCodes.NOT_UNIQUE.errorCode("depositReference"))));
  }

  @Test
  void validate_reference_unique_valid() {
    var form = PadPermanentDepositTestUtil.createDepositFormWithReference(1, "myRef2");
    var depositSameRef = PadPermanentDepositTestUtil.createDepositsWithReference(2, "myRef1");

    validationHints = PadPermanentDepositTestUtil.createValidationHintsWithDeposits(pwaApplicationDetail,
        List.of(depositSameRef));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(
        entry("depositReference", Set.of(FieldValidationErrorCodes.NOT_UNIQUE.errorCode("depositReference"))));
  }


  @Test
  void validate_startDate_noStartOfWorksDate_fromDateBeforeToday_invalid() {

    var startDate = TODAY.minusMonths(1L);
    var form = PadPermanentDepositTestUtil.createFormWithStartDate(startDate.getMonthValue(), startDate.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("fromDate.month", Set.of("month" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));
  }

  @Test
  void validate_startDate_noStartOfWorksDate_fromDateIsToday_valid() {

    var form = PadPermanentDepositTestUtil.createFormWithStartDate(TODAY.getMonthValue(), TODAY.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(
        entry("fromDate.month", Set.of("month" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));
  }

  @Test
  void validate_startDate_beforeStartOfWorksDate_invalid() {

    var startDate = TODAY.minusMonths(1L);
    var form = PadPermanentDepositTestUtil.createFormWithStartDate(startDate.getMonthValue(), startDate.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator, form,
        PadPermanentDepositTestUtil.createValidationHintsWithTimestamp(pwaApplicationDetail, TODAY_TS));
    assertThat(errorsMap).contains(
        entry("fromDate.month", Set.of("month" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));
  }

  @Test
  void validate_startDate_onStartOfWorksDate_valid() {

    var form = PadPermanentDepositTestUtil.createFormWithStartDate(TODAY.getMonthValue(), TODAY.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator, form,
        PadPermanentDepositTestUtil.createValidationHintsWithTimestamp(pwaApplicationDetail, TODAY_TS));
    assertThat(errorsMap).doesNotContain(
        entry("fromDate.month", Set.of("month" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));
  }

  @Test
  void validate_startDate_Future() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.getFromDate().setMonth(2);
    form.getFromDate().setYear(2120);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(
        entry("toDate.month", Set.of("toDate.month" + FieldValidationErrorCodes.BEFORE_TODAY.getCode())),
        entry("toDate.month", Set.of("month.month" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  void validate_dates_yearsTooBig() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.getFromDate().setMonth(2);
    form.getFromDate().setYear(4001);
    form.getToDate().setMonth(2);
    form.getToDate().setYear(4001);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("fromDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("toDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  void validate_dates_yearsTooSmall() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.getFromDate().setMonth(2);
    form.getFromDate().setYear(999);
    form.getToDate().setMonth(2);
    form.getToDate().setYear(999);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("fromDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("toDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())));
  }


  @Test
  void validate_toDate_beforeDepositStartDate_invalid() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.getFromDate().setMonth(TODAY.getMonthValue());
    form.getFromDate().setYear(TODAY.getYear());
    var toDate = TODAY.minusMonths(1);
    form.getToDate().setMonth(toDate.getMonthValue());
    form.getToDate().setYear(toDate.getYear());

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("toDate.month", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("month"))),
        entry("toDate.year", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("year"))));
  }

  @Test
  void validate_toDate_optionsAppType_afterMaxPeriodFromDepositStartDate_invalid() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.getFromDate().setMonth(TODAY.getMonthValue());
    form.getFromDate().setYear(TODAY.getYear());
    var toDate = TODAY.plusMonths(7);
    form.getToDate().setMonth(toDate.getMonthValue());
    form.getToDate().setYear(toDate.getYear());

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    validationHints = PadPermanentDepositTestUtil.createValidationHints(pwaApplicationDetail);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("toDate.month", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("month"))),
        entry("toDate.year", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("year"))));
  }

  @Test
  void validate_toDate_optionsAppType_exactlyAtMaxPeriodFromDepositStartDate_valid() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.getFromDate().setMonth(TODAY.getMonthValue());
    form.getFromDate().setYear(TODAY.getYear());
    var toDate = TODAY.plusMonths(6);
    form.getToDate().setMonth(toDate.getMonthValue());
    form.getToDate().setYear(toDate.getYear());

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    validationHints = PadPermanentDepositTestUtil.createValidationHints(pwaApplicationDetail);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(
        entry("toDate.month", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("month"))),
        entry("toDate.year", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("year"))));
  }

  @Test
  void validate_toDate_optionsAppType_withinMaxPeriodFromDepositStartDate_valid() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.getFromDate().setMonth(TODAY.getMonthValue());
    form.getFromDate().setYear(TODAY.getYear());
    var toDate = TODAY.plusMonths(5);
    form.getToDate().setMonth(toDate.getMonthValue());
    form.getToDate().setYear(toDate.getYear());

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    validationHints = PadPermanentDepositTestUtil.createValidationHints(pwaApplicationDetail);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(
        entry("toDate.month", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("month"))),
        entry("toDate.year", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("year"))));
  }

  @Test
  void validate_toDate_notOptionsAppType_afterMaxPeriodFromDepositStartDate_invalid() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.getFromDate().setMonth(TODAY.getMonthValue());
    form.getFromDate().setYear(TODAY.getYear());
    var toDate = TODAY.plusMonths(13);
    form.getToDate().setMonth(toDate.getMonthValue());
    form.getToDate().setYear(toDate.getYear());

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("toDate.month", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("month"))),
        entry("toDate.year", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("year"))));
  }

  @Test
  void validate_toDate_notOptionsAppType_exactlyAtMaxPeriodFromDepositStartDate_valid() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.getFromDate().setMonth(TODAY.getMonthValue());
    form.getFromDate().setYear(TODAY.getYear());
    var toDate = TODAY.plusMonths(12);
    form.getToDate().setMonth(toDate.getMonthValue());
    form.getToDate().setYear(toDate.getYear());

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(
        entry("toDate.month", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("month"))),
        entry("toDate.year", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("year"))));
  }

  @Test
  void validate_toDate_notOptionsAppType_withinMaxPeriodFromDepositStartDate_valid() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.getFromDate().setMonth(TODAY.getMonthValue());
    form.getFromDate().setYear(TODAY.getYear());
    var toDate = TODAY.plusMonths(11);
    form.getToDate().setMonth(toDate.getMonthValue());
    form.getToDate().setYear(toDate.getYear());

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(
        entry("toDate.month", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("month"))),
        entry("toDate.year", Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode("year"))));
  }

  @Test
  void validate_dates_nonePresent() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setFromDate(new TwoFieldDateInput());
    form.setToDate(new TwoFieldDateInput());

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("toDate.month", Set.of("month" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("toDate.year", Set.of("year" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_materialType_notSelected() {
    var form = getPermanentDepositsFormWithCoordinates();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("materialType", Set.of("materialType.required")));
  }


  @Test
  void validate_whenConcreteMattress_andNullData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setConcreteMattressLength(new DecimalInput(""));
    form.setConcreteMattressWidth(new DecimalInput(""));
    form.setConcreteMattressDepth(new DecimalInput(""));
    form.setQuantityConcrete(new DecimalInput(""));
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(errors).contains(
        entry(CONCRETE_MATTRESS_LENGTH_ATTR + ".value", Set.of("value.required")),
        entry(CONCRETE_MATTRESS_WIDTH_ATTR + ".value", Set.of("value.required")),
        entry(CONCRETE_MATTRESS_DEPTH_ATTR + ".value", Set.of("value.required"))
    );

  }

  @Test
  void validate_whenConcreteMattress_andValidData() {

    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setConcreteMattressLength(new DecimalInput(BigDecimal.TEN));
    form.setConcreteMattressWidth(new DecimalInput(BigDecimal.TEN));
    form.setConcreteMattressDepth(new DecimalInput(BigDecimal.TEN));
    form.setQuantityConcrete(new DecimalInput(BigDecimal.TEN));
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(errors).doesNotContainKeys(
        CONCRETE_MATTRESS_LENGTH_ATTR,
        CONCRETE_MATTRESS_WIDTH_ATTR,
        CONCRETE_MATTRESS_DEPTH_ATTR
    );

  }

  @Test
  void validate_whenConcreteMattress_andMaxDpExceeded() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setConcreteMattressLength(new DecimalInput(BigDecimal.valueOf(1.111)));
    form.setConcreteMattressWidth(new DecimalInput(BigDecimal.valueOf(1.111)));
    form.setConcreteMattressDepth(new DecimalInput(BigDecimal.valueOf(1.111)));
    form.setQuantityConcrete(new DecimalInput("42"));
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(errors).contains(
        entry(CONCRETE_MATTRESS_LENGTH_ATTR + ".value", Set.of("value.maxDpExceeded")),
        entry(CONCRETE_MATTRESS_WIDTH_ATTR + ".value", Set.of("value.maxDpExceeded")),
        entry(CONCRETE_MATTRESS_DEPTH_ATTR + ".value", Set.of("value.maxDpExceeded"))
    );

  }

  @Test
  void validate_concrete_invalidQuantity() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setQuantityConcrete(new DecimalInput("no num"));
    form.setConcreteMattressLength(new DecimalInput("42"));
    form.setConcreteMattressWidth(new DecimalInput("42"));
    form.setConcreteMattressDepth(new DecimalInput("42"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("quantityConcrete.value", Set.of("value.invalid")));
  }

  @Test
  void validate_concrete_contingencyTooBig() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setConcreteMattressLength(new DecimalInput("42"));
    form.setConcreteMattressWidth(new DecimalInput("42"));
    form.setConcreteMattressDepth(new DecimalInput("42"));
    form.setQuantityConcrete(new DecimalInput("42"));
    form.setContingencyConcreteAmount(StringUtils.repeat('a', 151));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("contingencyConcreteAmount", Set.of("contingencyConcreteAmount.maxLengthExceeded")));
  }

  @Test
  void validate_rocks_noSizeData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.ROCK);
    form.setQuantityRocks(new DecimalInput("42"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("rocksSize", Set.of("rocksSize.invalid")));
  }

  @Test
  void validate_rocksSize_lengthExceeded() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.ROCK);
    form.setQuantityRocks(new DecimalInput("42"));
    form.setRocksSize(ValidatorTestUtils.overCharLength(20));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("rocksSize", Set.of("rocksSize.maxLengthExceeded")));
  }

  @Test
  void validate_rocks_invalidQuantity() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.ROCK);
    form.setQuantityRocks(new DecimalInput("no num"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("quantityRocks.value", Set.of("value.invalid")));
  }

  @Test
  void validate_rocks_contingencyTooBig() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.ROCK);
    form.setQuantityRocks(new DecimalInput("no num"));
    form.setContingencyRocksAmount(StringUtils.repeat('a', 151));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("contingencyRocksAmount", Set.of("contingencyRocksAmount.maxLengthExceeded")));
  }

  @Test
  void validate_groutBags_noSizeData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setGroutBagsSize(new DecimalInput(""));
    form.setQuantityGroutBags(new DecimalInput(""));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("groutBagsSize.value", Set.of("value.required")));
  }

  @Test
  void validate_groutBags_invalidQuantity() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setQuantityGroutBags(new DecimalInput("no num"));
    form.setGroutBagsSize(new DecimalInput("42"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("quantityGroutBags.value", Set.of("value.invalid")));
  }

  @Test
  void validate_groutBags_bioDegradableNotSelected() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setGroutBagsSize(new DecimalInput("42"));
    form.setQuantityGroutBags(new DecimalInput("42"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("groutBagsBioDegradable", Set.of("groutBagsBioDegradable.required")));
  }

  @Test
  void validate_groutBags_bioDegradableNotUsedDescription_Blank() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setGroutBagsSize(new DecimalInput("42"));
    form.setQuantityGroutBags(new DecimalInput("42"));
    form.setGroutBagsBioDegradable(false);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("bioGroutBagsNotUsedDescription", Set.of("bioGroutBagsNotUsedDescription.blank")));
  }

  @Test
  void validate_groutBags_bioDegradableNotUsedDescription_tooBig() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setGroutBagsSize(new DecimalInput("42"));
    form.setQuantityGroutBags(new DecimalInput("42"));
    form.setGroutBagsBioDegradable(false);
    form.setBioGroutBagsNotUsedDescription(StringUtils.repeat('a', 5000));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("bioGroutBagsNotUsedDescription", Set.of("bioGroutBagsNotUsedDescription.maxLengthExceeded")));
  }

  @Test
  void validate_groutBags_contingencyTooBig() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setGroutBagsSize(new DecimalInput("42"));
    form.setQuantityGroutBags(new DecimalInput("42"));
    form.setContingencyGroutBagsAmount(StringUtils.repeat('a', 151));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("contingencyGroutBagsAmount", Set.of("contingencyGroutBagsAmount.maxLengthExceeded")));
  }

  @Test
  void validate_otherMaterial_otherMaterialType_notProvided() {
    PermanentDepositsForm form = getOtherPermanentDepositForm();
    form.setOtherMaterialType("");
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("otherMaterialType", Set.of("otherMaterialType.invalid")));
  }

  private PermanentDepositsForm getOtherPermanentDepositForm() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.OTHER);
    var today = LocalDate.now();
    form.setFromDate(new TwoFieldDateInput(today.getYear(), today.getMonthValue()));
    form.setToDate(new TwoFieldDateInput(today.getYear(), today.getMonth().plus(1).getValue()));
    form.setOtherMaterialType(StringUtils.repeat('a', 50));
    form.setQuantityOther(new DecimalInput("33"));
    return form;
  }

  @Test
  void validate_otherMaterial_otherMaterialType_sizeTooBig() {
    PermanentDepositsForm form = getOtherPermanentDepositForm();
    form.setOtherMaterialType(StringUtils.repeat('a', 51));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("otherMaterialType", Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("otherMaterialType"))));
  }

  @Test
  void validate_otherMaterial_otherMaterialType_sizeOk() {
    PermanentDepositsForm form = getOtherPermanentDepositForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContainKey("otherMaterialType");
  }

  @Test
  void validate_otherMaterial_noSizeData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.OTHER);
    form.setQuantityOther(new DecimalInput(""));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("otherMaterialSize", Set.of("otherMaterialSize.invalid")));
  }

  @Test
  void validate_otherSize_lengthExceeded() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.OTHER);
    form.setQuantityOther(new DecimalInput("42"));
    form.setOtherMaterialSize(ValidatorTestUtils.overCharLength(20));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("otherMaterialSize", Set.of("otherMaterialSize.maxLengthExceeded")));
  }


  @Test
  void validate_otherMaterial_invalidQuantity() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.OTHER);
    form.setQuantityOther(new DecimalInput("no num"));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("quantityOther.value", Set.of("value.invalid")));
  }

  @Test
  void validate_contingencyOtherAmount_tooBig() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.OTHER);
    form.setQuantityOther(new DecimalInput("42"));
    form.setQuantityRocks(new DecimalInput("no num"));
    form.setContingencyOtherAmount(RandomStringUtils.randomAlphabetic(151));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("contingencyOtherAmount", Set.of("contingencyOtherAmount.maxLengthExceeded")));
  }

  @Test
  void validate_fromLongitudeAndLatitude() {

    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();

    var fromCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(55, 55, BigDecimal.valueOf(55.55), LatitudeDirection.NORTH),
            new LongitudeCoordinate(12, 12, BigDecimal.valueOf(12), LongitudeDirection.EAST)
        ), fromCoordinateForm
    );
    form.setFromCoordinateForm(fromCoordinateForm);

    var toCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(46, 46, BigDecimal.valueOf(46), LatitudeDirection.SOUTH),
            new LongitudeCoordinate(6, 6, BigDecimal.valueOf(6.66), LongitudeDirection.WEST)
        ), toCoordinateForm
    );
    form.setToCoordinateForm(toCoordinateForm);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).doesNotContain(
        entry("fromCoordinateForm", Set.of("fromLocation.required")),
        entry("toCoordinateForm", Set.of("toLocation.required"))
    );
  }

  @Test
  void validate_footnote_lengthExceeded() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setFootnote(ValidatorTestUtils.overMaxDefaultCharLength());
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("footnote", Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("footnote"))));
  }


}