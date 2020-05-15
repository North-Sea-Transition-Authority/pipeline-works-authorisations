package uk.co.ogauthority.pwa.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.enums.ValueRequirement;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDepositRadioOption;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositValidatorTest {

  private PermanentDepositsValidator validator;

  @Before
  public void setUp() {
    validator = new PermanentDepositsValidator();
  }

  public PermanentDepositsForm getPermanentDepositsFormWithMaterialType(){
    var form = new PermanentDepositsForm();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    return form;
  }

  public PadProjectInformation getProjectInfoWithStartDate(){
    var padProjectInformation = new PadProjectInformation();
    padProjectInformation.setProposedStartTimestamp(LocalDate.of(2020, 3, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    return padProjectInformation;
  }

  public Map<String, Set<String>> getErrorMap(PermanentDepositsForm form, PadProjectInformation padProjectInformation) {
    var errors = new BeanPropertyBindingResult(form, "form");
    validator.validate(form, errors, padProjectInformation);
    return errors.getFieldErrors().stream()
        .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
  }

  @Test
  public void validate_fromDate_Null() {
    var form = getPermanentDepositsFormWithMaterialType();
    var padProjectInformation = new PadProjectInformation();
    padProjectInformation.setProposedStartTimestamp(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

    Map<String, Set<String>> errorsMap = getErrorMap(form, padProjectInformation);
    assertThat(errorsMap).contains(entry("fromMonth", Set.of("fromMonth.invalid")),
        entry("fromYear", Set.of("fromYear.invalid")));
  }

  @Test
  public void validate_fromDate_Past() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.setFromMonth(2);
    form.setFromYear(2020);
    var padProjectInformation = new PadProjectInformation();
    padProjectInformation.setProposedStartTimestamp(LocalDate.of(2020, 3, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

    Map<String, Set<String>> errorsMap = getErrorMap(form, padProjectInformation);
    assertThat(errorsMap).contains(entry("fromMonth", Set.of("fromMonth.beforeTarget")));
  }

  @Test
  public void validate_fromDate_Future() {
    var form = getPermanentDepositsFormWithMaterialType();
    form.setFromMonth(2);
    form.setFromYear(2020);
    var padProjectInformation = new PadProjectInformation();
    padProjectInformation.setProposedStartTimestamp(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

    Map<String, Set<String>> errorsMap = getErrorMap(form, padProjectInformation);
    assertThat(errorsMap).doesNotContain(entry("fromMonth", Set.of("fromMonth.beforeTarget")));
  }



  @Test
  public void validate_toDate_Null() {
    Map<String, Set<String>> errorsMap = getErrorMap(getPermanentDepositsFormWithMaterialType(), getProjectInfoWithStartDate());
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

    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
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

    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
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

    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
    assertThat(errorsMap).doesNotContain(entry("toMonth", Set.of("toMonth.outOfTargetRange")),
        entry("toYear", Set.of("toYear.outOfTargetRange")));
  }

  @Test
  public void validate_materialType_notSelected() {
    var form = new PermanentDepositsForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
    assertThat(errorsMap).contains(entry("materialType", Set.of("materialType.required")));
  }


  @Test
  public void validate_concrete_noSizeData() {
    var form = new PermanentDepositsForm();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
    assertThat(errorsMap).contains(entry("concreteMattressLength", Set.of("concreteMattressLength.invalid")),
        entry("concreteMattressWidth", Set.of("concreteMattressWidth.invalid")),
        entry("concreteMattressDepth", Set.of("concreteMattressDepth.invalid")));
  }

  @Test
  public void validate_concrete_invalidQuantity() {
    var form = new PermanentDepositsForm();
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setQuantityConcrete("no num");
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
    assertThat(errorsMap).contains(entry("quantityConcrete", Set.of("quantityConcrete.invalid")));
  }


  @Test
  public void validate_rocks_noSizeData() {
    var form = new PermanentDepositsForm();
    form.setMaterialType(MaterialType.ROCK);
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
    assertThat(errorsMap).contains(entry("rocksSize", Set.of("rocksSize.invalid")));
  }

  @Test
  public void validate_rocks_invalidQuantity() {
    var form = new PermanentDepositsForm();
    form.setMaterialType(MaterialType.ROCK);
    form.setQuantityRocks("no num");
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
    assertThat(errorsMap).contains(entry("quantityRocks", Set.of("quantityRocks.invalid")));
  }


  @Test
  public void validate_groutBags_noSizeData() {
    var form = new PermanentDepositsForm();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
    assertThat(errorsMap).contains(entry("groutBagsSize", Set.of("groutBagsSize.invalid")));
  }

  @Test
  public void validate_groutBags_invalidQuantity() {
    var form = new PermanentDepositsForm();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setQuantityRocks("no num");
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
    assertThat(errorsMap).contains(entry("quantityGroutBags", Set.of("quantityGroutBags.invalid")));
  }

  @Test
  public void validate_groutBags_bioDegradableNotSelected() {
    var form = new PermanentDepositsForm();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
    assertThat(errorsMap).contains(entry("groutBagsBioDegradable", Set.of("groutBagsBioDegradable.required")));
  }

  @Test
  public void validate_groutBags_bioDegradableNotUsedDescription_Blank() {
    var form = new PermanentDepositsForm();
    form.setMaterialType(MaterialType.GROUT_BAGS);
    form.setGroutBagsBioDegradable(false);
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
    assertThat(errorsMap).contains(entry("bioGroutBagsNotUsedDescription", Set.of("bioGroutBagsNotUsedDescription.blank")));
  }


  @Test
  public void validate_otherMaterial_noSizeData() {
    var form = new PermanentDepositsForm();
    form.setMaterialType(MaterialType.OTHER);
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
    assertThat(errorsMap).contains(entry("otherMaterialSize", Set.of("otherMaterialSize.invalid")));
  }

  @Test
  public void validate_otherMaterial_invalidQuantity() {
    var form = new PermanentDepositsForm();
    form.setMaterialType(MaterialType.OTHER);
    form.setQuantityRocks("no num");
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());
    assertThat(errorsMap).contains(entry("quantityOther", Set.of("quantityOther.invalid")));
  }

  @Test
  public void validate_fromLongitudeAndLatitude_invalid() {

    var form = getPermanentDepositsFormWithMaterialType();
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());

    assertThat(errorsMap).contains(
        entry("fromLatitudeDegrees", Set.of("fromLatitudeDegrees.required")),
        entry("fromLatitudeDegrees", Set.of("fromLatitudeDegrees.required")),
        entry("fromLatitudeDegrees", Set.of("fromLatitudeDegrees.required")),
        entry("fromLongitudeDegrees", Set.of("fromLongitudeDegrees.required")),
        entry("fromLongitudeMinutes", Set.of("fromLongitudeMinutes.required")),
        entry("fromLongitudeSeconds", Set.of("fromLongitudeSeconds.required")),
        entry("fromLongitudeDirection", Set.of("fromLongitudeDirection.required"))
    );
  }

  @Test
  public void validate_toLongitudeAndLatitude_invalid() {

    var form = getPermanentDepositsFormWithMaterialType();
    Map<String, Set<String>> errorsMap = getErrorMap(form, getProjectInfoWithStartDate());

    assertThat(errorsMap).contains(
        entry("toLatitudeDegrees", Set.of("toLatitudeDegrees.required")),
        entry("toLatitudeDegrees", Set.of("toLatitudeDegrees.required")),
        entry("toLatitudeDegrees", Set.of("toLatitudeDegrees.required")),
        entry("toLongitudeDegrees", Set.of("toLongitudeDegrees.required")),
        entry("toLongitudeMinutes", Set.of("toLongitudeMinutes.required")),
        entry("toLongitudeSeconds", Set.of("toLongitudeSeconds.required")),
        entry("toLongitudeDirection", Set.of("toLongitudeDirection.required"))
    );
  }

}