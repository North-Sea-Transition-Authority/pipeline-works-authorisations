package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentDataFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentFormValidator;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInputValidator;
import uk.co.ogauthority.pwa.util.validation.PipelineValidationUtils;

@RunWith(MockitoJUnitRunner.class)
public class PipelineIdentFormValidatorTest {

  private PipelineIdentFormValidator validator;

  @Spy
  private DecimalInputValidator decimalInputValidator;

  @Before
  public void setUp() {
    validator = new PipelineIdentFormValidator(new PipelineIdentDataFormValidator(decimalInputValidator), new CoordinateFormValidator(),
        decimalInputValidator);
  }

  @Test
  public void valid() {
    var result = ValidatorTestUtils.getFormValidationErrors(validator, buildForm(), (Object) null, PipelineCoreType.SINGLE_CORE);
    assertThat(result).isEmpty();
  }

  @Test
  public void failed_mandatory_definingStructure() {
    var form = new PipelineIdentForm();
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    form.setDataForm(new PipelineIdentDataForm());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).containsOnly(
        entry("fromLocation", Set.of("fromLocation.required")),
        entry("toLocation", Set.of("toLocation.required")),
        entry("definingStructure", Set.of("definingStructure.required")),
        entry("dataForm.componentPartsDescription", Set.of("componentPartsDescription.required")),
        entry("dataForm.productsToBeConveyed", Set.of("productsToBeConveyed.required"))
    );
  }

  @Test
  public void failed_mandatory_notDefiningStructure() {

    var form = createEmptyForm();
    form.setDefiningStructure(false);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).contains(
        entry("fromLocation", Set.of("fromLocation.required")),
        entry("toLocation", Set.of("toLocation.required")),
        entry("length.value", Set.of("value.required")),
        entry("dataForm.componentPartsDescription", Set.of("componentPartsDescription.required")),
        entry("dataForm.productsToBeConveyed", Set.of("productsToBeConveyed.required")),
        entry("dataForm.maop.value", Set.of("value.required")),
        entry("dataForm.externalDiameter.value", Set.of("value.required")),
        entry("dataForm.internalDiameter.value", Set.of("value.required")),
        entry("dataForm.wallThickness.value", Set.of("value.required")),
        entry("dataForm.insulationCoatingType", Set.of("insulationCoatingType.required"))
    );
  }

  @Test
  public void fromLocation_tooLong() {

    var form = buildForm();
    form.setFromLocation(StringUtils.repeat("a", 201));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).containsOnly(entry("fromLocation", Set.of("fromLocation.maxLengthExceeded")));

  }

  @Test
  public void fromLocation_charBoundary() {

    var form = buildForm();
    form.setFromLocation(StringUtils.repeat("a", 200));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).isEmpty();

  }

  @Test
  public void fromLocation_unwantedChars() {

    var form = buildForm();
    form.setFromLocation("bad##");

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).containsOnly(entry("fromLocation", Set.of("fromLocation.invalid")));

  }

  @Test
  public void fromLocation_notEqualWithToLocation_definingStructure() {

    var form = buildForm();
    form.setDefiningStructure(true);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).contains(entry("fromLocation", Set.of("fromLocation.invalid")));

  }

  @Test
  public void toLocation_tooLong() {

    var form = buildForm();
    form.setToLocation(StringUtils.repeat("a", 201));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).containsOnly(entry("toLocation", Set.of("toLocation.maxLengthExceeded")));

  }

  @Test
  public void toLocation_charBoundary() {

    var form = buildForm();
    form.setToLocation(StringUtils.repeat("a", 200));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).isEmpty();

  }

  @Test
  public void toLocation_unwantedChars() {

    var form = buildForm();
    form.setToLocation("bad##");

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).containsOnly(entry("toLocation", Set.of("toLocation.invalid")));

  }

  @Test
  public void fromLatitudeNotEqualWithToLatitude_definingStructure_noEqualityErrorWhenCoordinatesInvalid() {

    var form = buildForm();
    form.setDefiningStructure(true);
    form.getFromCoordinateForm().setLatitudeDegrees(null);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).doesNotContain(
        entry("fromCoordinateForm.latitudeDegrees", Set.of("fromCoordinateForm.latitudeDegrees.invalid")),
        entry("fromCoordinateForm.latitudeMinutes", Set.of("fromCoordinateForm.latitudeMinutes.invalid")),
        entry("fromCoordinateForm.latitudeSeconds", Set.of("fromCoordinateForm.latitudeSeconds.invalid")),
        entry("toCoordinateForm.latitudeDegrees", Set.of("toCoordinateForm.latitudeDegrees.invalid")),
        entry("toCoordinateForm.latitudeMinutes", Set.of("toCoordinateForm.latitudeMinutes.invalid")),
        entry("toCoordinateForm.latitudeSeconds", Set.of("toCoordinateForm.latitudeSeconds.invalid")));
  }

  @Test
  public void fromLatitudeNotEqualWithToLatitude_definingStructure() {

    var form = buildForm();
    form.setDefiningStructure(true);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).contains(entry("fromCoordinateForm.latitudeDegrees", Set.of("fromCoordinateForm.latitudeDegrees.invalid")));
    assertThat(result).contains(entry("fromCoordinateForm.latitudeMinutes", Set.of("fromCoordinateForm.latitudeMinutes.invalid")));
    assertThat(result).contains(entry("fromCoordinateForm.latitudeSeconds", Set.of("fromCoordinateForm.latitudeSeconds.invalid")));
    assertThat(result).contains(entry("toCoordinateForm.latitudeDegrees", Set.of("toCoordinateForm.latitudeDegrees.invalid")));
    assertThat(result).contains(entry("toCoordinateForm.latitudeMinutes", Set.of("toCoordinateForm.latitudeMinutes.invalid")));
    assertThat(result).contains(entry("toCoordinateForm.latitudeSeconds", Set.of("toCoordinateForm.latitudeSeconds.invalid")));

  }

  @Test
  public void fromLongitudeNotEqualWithToLongitude_definingStructure_noEqualityErrorWhenCoordinatesInvalid() {

    var form = buildForm();
    form.setDefiningStructure(true);
    form.getFromCoordinateForm().setLongitudeDegrees(null);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).doesNotContain(
        entry("fromCoordinateForm.longitudeDegrees", Set.of("fromCoordinateForm.longitudeDegrees.invalid")),
        entry("fromCoordinateForm.longitudeMinutes", Set.of("fromCoordinateForm.longitudeMinutes.invalid")),
        entry("fromCoordinateForm.longitudeSeconds", Set.of("fromCoordinateForm.longitudeSeconds.invalid")),
        entry("toCoordinateForm.longitudeDegrees", Set.of("toCoordinateForm.longitudeDegrees.invalid")),
        entry("toCoordinateForm.longitudeMinutes", Set.of("toCoordinateForm.longitudeMinutes.invalid")),
        entry("toCoordinateForm.longitudeSeconds", Set.of("toCoordinateForm.longitudeSeconds.invalid")));
  }

  @Test
  public void fromLongitudeNotEqualWithToLongitude_definingStructure() {

    var form = buildForm();
    form.setDefiningStructure(true);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).contains(entry("fromCoordinateForm.longitudeDegrees", Set.of("fromCoordinateForm.longitudeDegrees.invalid")));
    assertThat(result).contains(entry("fromCoordinateForm.longitudeMinutes", Set.of("fromCoordinateForm.longitudeMinutes.invalid")));
    assertThat(result).contains(entry("fromCoordinateForm.longitudeSeconds", Set.of("fromCoordinateForm.longitudeSeconds.invalid")));
    assertThat(result).contains(entry("toCoordinateForm.longitudeDegrees", Set.of("toCoordinateForm.longitudeDegrees.invalid")));
    assertThat(result).contains(entry("toCoordinateForm.longitudeMinutes", Set.of("toCoordinateForm.longitudeMinutes.invalid")));
    assertThat(result).contains(entry("toCoordinateForm.longitudeSeconds", Set.of("toCoordinateForm.longitudeSeconds.invalid")));

  }


  @Test
  public void length_notPositive() {

    var form = buildForm();

    form.setLength(new DecimalInput(BigDecimal.valueOf(-1)));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).containsOnly(entry("length.value", Set.of("value.invalid")));

  }

  @Test
  public void length_over2Dp() {

    var form = buildForm();

    form.setLength(new DecimalInput(BigDecimal.valueOf(1.323)));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).containsOnly(entry("length.value", Set.of("value.maxDpExceeded")));

  }

  @Test
  //covers a unique edge-case where 'defining structure' selected & invalid optional length entered,
  // switch to 'not defining structure' and submit. We should ignore the invalid length from 'defining structure' nested field
  public void validate_notDefiningStructureOptionSelected_invalidDefiningStructureLength_noError() {

    var form = buildForm();

    form.setLengthOptional(new DecimalInput("invalid num"));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE);

    assertThat(result).doesNotContain(entry("lengthOptional.value", Set.of("value" + FieldValidationErrorCodes.INVALID.getCode())));

  }

  private PipelineIdentForm buildForm() {

    var form = createEmptyForm();

    form.setFromLocation("from");
    var fromCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(55, 55, BigDecimal.valueOf(55.55), LatitudeDirection.NORTH),
            new LongitudeCoordinate(12, 12, new BigDecimal("12.00"), LongitudeDirection.EAST)
        ), fromCoordinateForm
    );
    form.setFromCoordinateForm(fromCoordinateForm);

    form.setToLocation("to");
    var toCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(46, 46, new BigDecimal("46.00"), LatitudeDirection.SOUTH),
            new LongitudeCoordinate(6, 6, BigDecimal.valueOf(6.66), LongitudeDirection.WEST)
        ), toCoordinateForm
    );
    form.setToCoordinateForm(toCoordinateForm);
    form.setDefiningStructure(false);
    form.setLength(new DecimalInput(BigDecimal.valueOf(65.55)));

    var dataForm = new PipelineIdentDataForm();
    dataForm.setExternalDiameter(new DecimalInput(BigDecimal.valueOf(12.1)));
    dataForm.setInternalDiameter(new DecimalInput(dataForm.getExternalDiameter().createBigDecimalOrNull().subtract(BigDecimal.ONE)));
    dataForm.setWallThickness(new DecimalInput(BigDecimal.valueOf(12.1)));
    dataForm.setMaop(new DecimalInput(BigDecimal.valueOf(12.1)));
    dataForm.setProductsToBeConveyed("prod");
    dataForm.setComponentPartsDescription("component");
    dataForm.setInsulationCoatingType("ins");
    form.setDataForm(dataForm);

    return form;

  }

  private PipelineIdentForm createEmptyForm() {

    var form = new PipelineIdentForm();
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    form.setLength(new DecimalInput());
    form.setLengthOptional(new DecimalInput());

    var dataForm = PipelineValidationUtils.createEmptyPipelineIdentDataForm();
    form.setDataForm(dataForm);

    return form;
  }


}
