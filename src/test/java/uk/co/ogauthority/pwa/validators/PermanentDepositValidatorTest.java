package uk.co.ogauthority.pwa.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
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
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositValidatorTest {

  private PermanentDepositsValidator validator;
  @Mock
  private PermanentDepositService service;

  @Before
  public void setUp() {
    validator = new PermanentDepositsValidator(new CoordinateFormValidator());
  }

  public PermanentDepositsForm getPermanentDepositsFormWithMaterialType(){
    var form = new PermanentDepositsForm();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    return form;
  }

  public PermanentDepositsForm getPermanentDepositsFormWithCoordinates(){
    var form = new PermanentDepositsForm();
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
  public void validate_fromDate_Null() {
    var form = getPermanentDepositsFormWithMaterialType();
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("fromMonth", Set.of("fromMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("fromYear", Set.of("fromYear" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  public void validate_fromDate_Past() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.setFromMonth(2);
    form.setFromYear(2020);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("fromMonth", Set.of("fromMonth" + FieldValidationErrorCodes.BEFORE_TODAY.getCode())));
  }

  @Test
  public void validate_fromDate_Future() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.setFromMonth(2);
    form.setFromYear(2120);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).doesNotContain(entry("fromMonth", Set.of("fromMonth" + FieldValidationErrorCodes.BEFORE_TODAY.getCode())),
        entry("fromMonth", Set.of("fromMonth" + FieldValidationErrorCodes.INVALID.getCode())));
  }



  @Test
  public void validate_toDate_Null() {
    Map<String, Set<String>> errorsMap = getErrorMap(getPermanentDepositsFormWithMaterialType());
    assertThat(errorsMap).contains(entry("toMonth", Set.of("toMonth.invalid")),
        entry("toYear", Set.of("toYear.invalid")));
  }

  @Test
  public void validate_toDate_Past() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.setFromMonth(2);
    form.setFromYear(2020);
    form.setToMonth(1);
    form.setToYear(2020);

    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("toMonth", Set.of("toMonth.outOfTargetRange")),
        entry("toYear", Set.of("toYear.outOfTargetRange")));
  }

  @Test
  public void validate_toDate_Future() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.setFromMonth(2);
    form.setFromYear(2020);
    form.setToMonth(3);
    form.setToYear(2021);

    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("toMonth", Set.of("toMonth.outOfTargetRange")),
        entry("toYear", Set.of("toYear.outOfTargetRange")));
  }

  @Test
  public void validate_toDate_Within() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.setFromMonth(2);
    form.setFromYear(2020);
    form.setToMonth(8);
    form.setToYear(2020);

    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).doesNotContain(entry("toMonth", Set.of("toMonth.outOfTargetRange")),
        entry("toYear", Set.of("toYear.outOfTargetRange")));
  }

  @Test
  public void validate_materialType_notSelected() {
    var form = getPermanentDepositsFormWithCoordinates();
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("materialType", Set.of("materialType.required")));
  }


  @Test
  public void validate_concrete_noSizeData() {
    var form = getPermanentDepositsFormWithCoordinates();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("concreteMattressLength", Set.of("concreteMattressLength.invalid")),
        entry("concreteMattressWidth", Set.of("concreteMattressWidth.invalid")),
        entry("concreteMattressDepth", Set.of("concreteMattressDepth.invalid")));
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

}