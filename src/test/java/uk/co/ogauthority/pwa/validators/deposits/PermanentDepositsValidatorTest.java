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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDepositTestUtil;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositsValidatorTest {

  private final String CONCRETE_MATTRESS_LENGTH_ATTR = "concreteMattressLength";
  private final String CONCRETE_MATTRESS_WIDTH_ATTR = "concreteMattressWidth";
  private final String CONCRETE_MATTRESS_DEPTH_ATTR = "concreteMattressDepth";

  private final static Instant TODAY_TS = Instant.now();
  private final static LocalDate TODAY = DateUtils.instantToLocalDate(TODAY_TS);

  private PermanentDepositsValidator validator;

  private PermanentDepositsValidationHints validationHints;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    validator = new PermanentDepositsValidator(new TwoFieldDateInputValidator(), new CoordinateFormValidator());

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    validationHints = PadPermanentDepositTestUtil.createValidationHints(pwaApplicationDetail);
  }

  public PermanentDepositsForm getPermanentDepositsFormWithCoordinates(){
    var form = new PermanentDepositsForm();
    form.setFromDate(new TwoFieldDateInput());
    form.setToDate(new TwoFieldDateInput());
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    return form;
  }


  @Test
  public void validate_consentedPipelinesAndOtherAppQuestionsBothAnsweredNo() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositIsForConsentedPipeline(false);
    form.setDepositIsForPipelinesOnOtherApp(false);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("depositIsForConsentedPipeline", Set.of("depositIsForConsentedPipeline" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("depositIsForPipelinesOnOtherApp", Set.of("depositIsForPipelinesOnOtherApp" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  public void validate_consentedPipelinesNull() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("depositIsForConsentedPipeline", Set.of("depositIsForConsentedPipeline" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_otherAppQuestionNull() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("depositIsForPipelinesOnOtherApp", Set.of("depositIsForPipelinesOnOtherApp" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_consentedPipelinesAnsweredYes_pipelinesNotProvided() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositIsForConsentedPipeline(true);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("selectedPipelines", Set.of("selectedPipelines" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_otherAppQuestionAnsweredYes_appRefAndNumNotProvided() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositIsForPipelinesOnOtherApp(true);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("appRefAndPipelineNum", Set.of("appRefAndPipelineNum" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_otherAppQuestionAnsweredYes_appRefAndNumNotProvided_lengthExceeded() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositIsForPipelinesOnOtherApp(true);
    form.setAppRefAndPipelineNum(ValidatorTestUtils.overMaxDefaultCharLength());
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("appRefAndPipelineNum", Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("appRefAndPipelineNum"))));
  }

  public void supports_whenSupported() {
    assertThat(validator.supports(PermanentDepositsForm.class)).isTrue();
  }

  @Test
  public void supports_whenNotSupported() {
    assertThat(validator.supports(Object.class)).isFalse();
  }


  @Test
  public void validate_reference_blank() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("depositReference", Set.of("depositReference" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_reference_tooLong_invalid() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositReference(StringUtils.repeat('d', 51));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("depositReference", Set.of("depositReference" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

  @Test
  public void validate_reference_maxAllowableLength_valid() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.setDepositReference(StringUtils.repeat('d', 50));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContainKeys("depositReference");
  }

  @Test
  public void validate_reference_notUnique_invalid() {
    var form = PadPermanentDepositTestUtil.createDepositFormWithReference(1, "myRef");
    var depositSameRef = PadPermanentDepositTestUtil.createDepositsWithReference(2, "myRef");

    validationHints = PadPermanentDepositTestUtil.createValidationHintsWithDeposits(pwaApplicationDetail, List.of(depositSameRef));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("depositReference", Set.of(FieldValidationErrorCodes.NOT_UNIQUE.errorCode("depositReference"))));
  }

  @Test
  public void validate_reference_notUnique_sameDeposit_valid() {
    var form = PadPermanentDepositTestUtil.createDepositFormWithReference(1, "myRef");
    var depositSameRef = PadPermanentDepositTestUtil.createDepositsWithReference(1, "myRef");

    validationHints = PadPermanentDepositTestUtil.createValidationHintsWithDeposits(pwaApplicationDetail, List.of(depositSameRef));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(
        entry("depositReference", Set.of(FieldValidationErrorCodes.NOT_UNIQUE.errorCode("depositReference"))));
  }

  @Test
  public void validate_reference_unique_valid() {
    var form = PadPermanentDepositTestUtil.createDepositFormWithReference(1, "myRef2");
    var depositSameRef = PadPermanentDepositTestUtil.createDepositsWithReference(2, "myRef1");

    validationHints = PadPermanentDepositTestUtil.createValidationHintsWithDeposits(pwaApplicationDetail, List.of(depositSameRef));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(
        entry("depositReference", Set.of(FieldValidationErrorCodes.NOT_UNIQUE.errorCode("depositReference"))));
  }
  

  @Test
  public void validate_startDate_noStartOfWorksDate_fromDateBeforeToday_invalid() {

    var startDate = TODAY.minusMonths(1L);
    var form = PadPermanentDepositTestUtil.createFormWithStartDate(startDate.getMonthValue(), startDate.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("fromDate.month", Set.of("month" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));
  }

  @Test
  public void validate_startDate_noStartOfWorksDate_fromDateIsToday_valid() {

    var form = PadPermanentDepositTestUtil.createFormWithStartDate(TODAY.getMonthValue(), TODAY.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(entry("fromDate.month", Set.of("month" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));
  }

  @Test
  public void validate_startDate_beforeStartOfWorksDate_invalid() {

    var startDate = TODAY.minusMonths(1L);
    var form = PadPermanentDepositTestUtil.createFormWithStartDate(startDate.getMonthValue(), TODAY.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator, form, PadPermanentDepositTestUtil.createValidationHintsWithTimestamp(pwaApplicationDetail, TODAY_TS));
    assertThat(errorsMap).contains(entry("fromDate.month", Set.of("month" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));
  }

  @Test
  public void validate_startDate_onStartOfWorksDate_valid() {

    var form = PadPermanentDepositTestUtil.createFormWithStartDate(TODAY.getMonthValue(), TODAY.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator, form, PadPermanentDepositTestUtil.createValidationHintsWithTimestamp(pwaApplicationDetail, TODAY_TS));
    assertThat(errorsMap).doesNotContain(entry("fromDate.month", Set.of("month" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));
  }

  @Test
  public void validate_startDate_Future() {
    var form = PadPermanentDepositTestUtil.createDefaultDepositForm();
    form.getFromDate().setMonth(2);
    form.getFromDate().setYear(2120);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).doesNotContain(entry("toDate.month", Set.of("toDate.month" + FieldValidationErrorCodes.BEFORE_TODAY.getCode())),
        entry("toDate.month", Set.of("month.month" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  public void validate_dates_yearsTooBig() {
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
  public void validate_dates_yearsTooSmall() {
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
  public void validate_toDate_beforeDepositStartDate_invalid() {
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
  public void validate_toDate_optionsAppType_afterMaxPeriodFromDepositStartDate_invalid() {
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
  public void validate_toDate_optionsAppType_exactlyAtMaxPeriodFromDepositStartDate_valid() {
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
  public void validate_toDate_optionsAppType_withinMaxPeriodFromDepositStartDate_valid() {
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
  public void validate_toDate_notOptionsAppType_afterMaxPeriodFromDepositStartDate_invalid() {
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
  public void validate_toDate_notOptionsAppType_exactlyAtMaxPeriodFromDepositStartDate_valid() {
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
  public void validate_toDate_notOptionsAppType_withinMaxPeriodFromDepositStartDate_valid() {
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
  public void validate_dates_nonePresent() {
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
  public void validate_materialType_notSelected() {
    var form = getPermanentDepositsFormWithCoordinates();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("materialType", Set.of("materialType.required")));
  }


  @Test
  public void validate_whenConcreteMattress_andNullData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(errors).contains(
        entry(CONCRETE_MATTRESS_LENGTH_ATTR,
            Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(CONCRETE_MATTRESS_LENGTH_ATTR))),
        entry(CONCRETE_MATTRESS_WIDTH_ATTR,
            Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(CONCRETE_MATTRESS_WIDTH_ATTR))),
        entry(CONCRETE_MATTRESS_DEPTH_ATTR,
            Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(CONCRETE_MATTRESS_DEPTH_ATTR)))
    );

  }

  @Test
  public void validate_whenConcreteMattress_andValidData() {

    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setConcreteMattressLength(BigDecimal.ONE);
    form.setConcreteMattressWidth(BigDecimal.TEN);
    form.setConcreteMattressDepth(BigDecimal.TEN);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(errors).doesNotContainKeys(
        CONCRETE_MATTRESS_LENGTH_ATTR,
        CONCRETE_MATTRESS_WIDTH_ATTR,
        CONCRETE_MATTRESS_DEPTH_ATTR
    );

  }

  @Test
  public void validate_whenConcreteMattress_andMaxDpExceeded() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setConcreteMattressLength(BigDecimal.valueOf(1.111));
    form.setConcreteMattressWidth(BigDecimal.valueOf(1.111));
    form.setConcreteMattressDepth(BigDecimal.valueOf(1.111));
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(errors).contains(
        entry(CONCRETE_MATTRESS_LENGTH_ATTR,
            Set.of(FieldValidationErrorCodes.MAX_DP_EXCEEDED.errorCode(CONCRETE_MATTRESS_LENGTH_ATTR))),
        entry(CONCRETE_MATTRESS_WIDTH_ATTR,
            Set.of(FieldValidationErrorCodes.MAX_DP_EXCEEDED.errorCode(CONCRETE_MATTRESS_WIDTH_ATTR))),
        entry(CONCRETE_MATTRESS_DEPTH_ATTR,
            Set.of(FieldValidationErrorCodes.MAX_DP_EXCEEDED.errorCode(CONCRETE_MATTRESS_DEPTH_ATTR)))
    );

  }

  @Test
  public void validate_concrete_invalidQuantity() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setQuantityConcrete("no num");
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("quantityConcrete", Set.of("quantityConcrete.invalid")));
  }

  @Test
  public void validate_concrete_contingencyTooBig() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setContingencyConcreteAmount(StringUtils.repeat('a', 151));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("contingencyConcreteAmount", Set.of("contingencyConcreteAmount.maxLengthExceeded")));
  }

  @Test
  public void validate_rocks_noSizeData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.ROCK);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("rocksSize", Set.of("rocksSize.invalid")));
  }

  @Test
  public void validate_rocks_invalidQuantity() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.ROCK);
    form.setQuantityRocks("no num");
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("quantityRocks", Set.of("quantityRocks.invalid")));
  }

  @Test
  public void validate_rocks_contingencyTooBig() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.ROCK);
    form.setQuantityRocks("qq");
    form.setContingencyRocksAmount(StringUtils.repeat('a', 151));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("contingencyRocksAmount", Set.of("contingencyRocksAmount.maxLengthExceeded")));
  }

  @Test
  public void validate_groutBags_noSizeData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("groutBagsSize", Set.of("groutBagsSize.invalid")));
  }

  @Test
  public void validate_groutBags_invalidQuantity() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setQuantityRocks("no num");
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("quantityGroutBags", Set.of("quantityGroutBags.invalid")));
  }

  @Test
  public void validate_groutBags_bioDegradableNotSelected() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("groutBagsBioDegradable", Set.of("groutBagsBioDegradable.required")));
  }

  @Test
  public void validate_groutBags_bioDegradableNotUsedDescription_Blank() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setGroutBagsBioDegradable(false);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("bioGroutBagsNotUsedDescription", Set.of("bioGroutBagsNotUsedDescription.blank")));
  }

  @Test
  public void validate_groutBags_bioDegradableNotUsedDescription_tooBig() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setGroutBagsBioDegradable(false);
    form.setBioGroutBagsNotUsedDescription(StringUtils.repeat('a', 5000));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("bioGroutBagsNotUsedDescription", Set.of("bioGroutBagsNotUsedDescription.maxLengthExceeded")));
  }

  @Test
  public void validate_groutBags_contingencyTooBig() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setContingencyGroutBagsAmount(StringUtils.repeat('a', 151));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("contingencyGroutBagsAmount", Set.of("contingencyGroutBagsAmount.maxLengthExceeded")));
  }

  @Test
  public void validate_otherMaterial_noSizeData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.OTHER);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("otherMaterialSize", Set.of("otherMaterialSize.invalid")));
  }

  @Test
  public void validate_otherMaterial_invalidQuantity() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.OTHER);
    form.setQuantityRocks("no num");
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("quantityOther", Set.of("quantityOther.invalid")));
  }

  @Test
  public void validate_contingencyOtherAmount_tooBig() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.OTHER);
    form.setQuantityRocks("no num");
    form.setContingencyOtherAmount(RandomStringUtils.randomAlphabetic(151));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(entry("contingencyOtherAmount", Set.of("contingencyOtherAmount.maxLengthExceeded")));
  }

  @Test
  public void validate_fromLongitudeAndLatitude() {

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
  public void validate_footnote_lengthExceeded() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setFootnote(ValidatorTestUtils.overMaxDefaultCharLength());
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(errorsMap).contains(
        entry("footnote", Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("footnote"))));
  }


}