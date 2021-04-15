package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositsValidatorTest {


  private final String CONCRETE_MATTRESS_LENGTH_ATTR = "concreteMattressLength";
  private final String CONCRETE_MATTRESS_WIDTH_ATTR = "concreteMattressWidth";
  private final String CONCRETE_MATTRESS_DEPTH_ATTR = "concreteMattressDepth";


  private PermanentDepositsValidator validator;
  @Mock
  private PermanentDepositService service;

  @Before
  public void setUp() {
    validator = new PermanentDepositsValidator(new TwoFieldDateInputValidator(), new CoordinateFormValidator());
  }

  public PermanentDepositsForm getPermanentDepositsFormWithMaterialType(){
    var form = new PermanentDepositsForm();
    form.setFromDate(new TwoFieldDateInput());
    form.setToDate(new TwoFieldDateInput());
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    return form;
  }

  public PermanentDepositsForm getPermanentDepositsFormWithCoordinates(){
    var form = new PermanentDepositsForm();
    form.setFromDate(new TwoFieldDateInput());
    form.setToDate(new TwoFieldDateInput());
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    return form;
  }

  public Map<String, Set<String>> getErrorMap(PermanentDepositsForm form) {
    var errors = new BeanPropertyBindingResult(form, "form");
    validator.validate(form, errors, service, new PwaApplicationDetail());
    return errors.getFieldErrors().stream()
        .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
  }

  @Test
  public void validate_consentedPipelinesAndOtherAppQuestionsBothAnsweredNo() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.setDepositIsForConsentedPipeline(false);
    form.setDepositIsForPipelinesOnOtherApp(false);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(
        entry("depositIsForConsentedPipeline", Set.of("depositIsForConsentedPipeline" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("depositIsForPipelinesOnOtherApp", Set.of("depositIsForPipelinesOnOtherApp" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  public void validate_consentedPipelinesNull() {
    var form = getPermanentDepositsFormWithMaterialType();
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("depositIsForConsentedPipeline", Set.of("depositIsForConsentedPipeline" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_otherAppQuestionNull() {
    var form = getPermanentDepositsFormWithMaterialType();
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("depositIsForPipelinesOnOtherApp", Set.of("depositIsForPipelinesOnOtherApp" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_consentedPipelinesAnsweredYes_pipelinesNotProvided() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.setDepositIsForConsentedPipeline(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("selectedPipelines", Set.of("selectedPipelines" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_otherAppQuestionAnsweredYes_appRefAndNumNotProvided() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.setDepositIsForPipelinesOnOtherApp(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(
        entry("appRefAndPipelineNum", Set.of("appRefAndPipelineNum" + FieldValidationErrorCodes.REQUIRED.getCode())));
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
    var form = getPermanentDepositsFormWithMaterialType();
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("depositReference", Set.of("depositReference" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_reference_notUnique() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.setDepositReference("myRef");
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("depositReference", Set.of("depositReference" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }


  @Test
  public void validate_fromDate_Past() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.getFromDate().setMonth(2);
    form.getFromDate().setYear(2020);
    form.setToDate(new TwoFieldDateInput(2020, 8));
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("fromDate.month", Set.of("month" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));
  }

  @Test
  public void validate_fromDate_Future() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.getFromDate().setMonth(2);
    form.getFromDate().setYear(2120);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).doesNotContain(entry("toDate.month", Set.of("toDate.month" + FieldValidationErrorCodes.BEFORE_TODAY.getCode())),
        entry("toDate.month", Set.of("month.month" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  public void validate_dates_yearsTooBig() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.getFromDate().setMonth(2);
    form.getFromDate().setYear(4001);
    form.getToDate().setMonth(2);
    form.getToDate().setYear(4001);

    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(
        entry("fromDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("toDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  public void validate_dates_yearsTooSmall() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.getFromDate().setMonth(2);
    form.getFromDate().setYear(999);
    form.getToDate().setMonth(2);
    form.getToDate().setYear(999);

    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(
        entry("fromDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("toDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())));
  }




  @Test
  public void validate_toDate_Past() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.getFromDate().setMonth(2);
    form.getFromDate().setYear(3020);
    form.getToDate().setMonth(1);
    form.getToDate().setYear(2020);

    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains((entry("toDate.month", Set.of("month.outOfTargetRange", "month.afterDate"))));
  }

  @Test
  public void validate_toDate_Future() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.getFromDate().setMonth(2);
    form.getFromDate().setYear(3020);
    form.getToDate().setMonth(3);
    form.getToDate().setYear(3021);

    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("toDate.month", Set.of("month.outOfTargetRange")));
  }

  @Test
  public void validate_toDate_Within() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.getFromDate().setMonth(2);
    form.getFromDate().setYear(2020);
    form.getToDate().setMonth(8);
    form.getToDate().setYear(2020);

    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).doesNotContain(entry("toDate.month", Set.of("toDate.month.outOfTargetRange")),
        entry("toDate.year", Set.of("toYear.outOfTargetRange")));
  }

  @Test
  public void validate_dates_nonePresent() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.setFromDate(new TwoFieldDateInput());

    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(
        entry("toDate.month", Set.of("month" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("toDate.year", Set.of("year" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }




  @Test
  public void validate_materialType_notSelected() {
    var form = getPermanentDepositsFormWithCoordinates();
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("materialType", Set.of("materialType.required")));
  }


  @Test
  public void validate_whenConcreteMattress_andNullData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

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
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

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
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

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
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("quantityConcrete", Set.of("quantityConcrete.invalid")));
  }


  @Test
  public void validate_rocks_noSizeData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.ROCK);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("rocksSize", Set.of("rocksSize.invalid")));
  }

  @Test
  public void validate_rocks_invalidQuantity() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.ROCK);
    form.setQuantityRocks("no num");
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("quantityRocks", Set.of("quantityRocks.invalid")));
  }


  @Test
  public void validate_groutBags_noSizeData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("groutBagsSize", Set.of("groutBagsSize.invalid")));
  }

  @Test
  public void validate_groutBags_invalidQuantity() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setQuantityRocks("no num");
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("quantityGroutBags", Set.of("quantityGroutBags.invalid")));
  }

  @Test
  public void validate_groutBags_bioDegradableNotSelected() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("groutBagsBioDegradable", Set.of("groutBagsBioDegradable.required")));
  }

  @Test
  public void validate_groutBags_bioDegradableNotUsedDescription_Blank() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setGroutBagsBioDegradable(false);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("bioGroutBagsNotUsedDescription", Set.of("bioGroutBagsNotUsedDescription.blank")));
  }


  @Test
  public void validate_otherMaterial_noSizeData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.OTHER);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("otherMaterialSize", Set.of("otherMaterialSize.invalid")));
  }

  @Test
  public void validate_otherMaterial_invalidQuantity() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.OTHER);
    form.setQuantityRocks("no num");
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("quantityOther", Set.of("quantityOther.invalid")));
  }

  @Test
  public void validate_fromLongitudeAndLatitude() {

    var form = getPermanentDepositsFormWithMaterialType();

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
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null);
    assertThat(result).doesNotContain(
        entry("fromCoordinateForm", Set.of("fromLocation.required")),
        entry("toCoordinateForm", Set.of("toLocation.required"))
    );
  }

  @Test
  public void validate_footnote_lengthExceeded() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setFootnote(ValidatorTestUtils.over4000Chars());
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(
        entry("footnote", Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("footnote"))));
  }


}