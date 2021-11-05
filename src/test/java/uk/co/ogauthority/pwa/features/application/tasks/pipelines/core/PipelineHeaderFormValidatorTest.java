package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineFlexibility;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinateUtils;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeDirection;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

public class PipelineHeaderFormValidatorTest {

  private PipelineHeaderFormValidator validator;

  private PipelineHeaderValidationHints validationHints;

  private static Boolean DO_NOT_VALIDATE_SEABED_QUESTION = false;
  private static Boolean VALIDATE_SEABED_QUESTION = true;

  @Before
  public void setUp() {
    validator = new PipelineHeaderFormValidator(new CoordinateFormValidator());
    validationHints = new PipelineHeaderValidationHints(
        PipelineStatus.IN_SERVICE,
        DO_NOT_VALIDATE_SEABED_QUESTION
    );
  }

  @Test
  public void valid() {
    var result = ValidatorTestUtils.getFormValidationErrors(validator, buildForm(), validationHints);
    assertThat(result).isEmpty();
  }

  @Test
  public void failed_mandatory() {
    var form = new PipelineHeaderForm();
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(result).contains(
        entry("fromLocation", Set.of("fromLocation.required")),
        entry("toLocation", Set.of("toLocation.required")),
        entry("fromCoordinateForm.latitudeDegrees", Set.of("fromCoordinateForm.latitudeDegrees.required")),
        entry("fromCoordinateForm.latitudeMinutes", Set.of("fromCoordinateForm.latitudeMinutes.required")),
        entry("fromCoordinateForm.latitudeSeconds", Set.of("fromCoordinateForm.latitudeSeconds.required")),
        entry("fromCoordinateForm.longitudeDegrees", Set.of("fromCoordinateForm.longitudeDegrees.required")),
        entry("fromCoordinateForm.longitudeMinutes", Set.of("fromCoordinateForm.longitudeMinutes.required")),
        entry("fromCoordinateForm.longitudeSeconds", Set.of("fromCoordinateForm.longitudeSeconds.required")),
        entry("fromCoordinateForm.longitudeDirection", Set.of("fromCoordinateForm.longitudeDirection.required")),
        entry("toCoordinateForm.latitudeDegrees", Set.of("toCoordinateForm.latitudeDegrees.required")),
        entry("toCoordinateForm.latitudeMinutes", Set.of("toCoordinateForm.latitudeMinutes.required")),
        entry("toCoordinateForm.latitudeSeconds", Set.of("toCoordinateForm.latitudeSeconds.required")),
        entry("toCoordinateForm.longitudeDegrees", Set.of("toCoordinateForm.longitudeDegrees.required")),
        entry("toCoordinateForm.longitudeMinutes", Set.of("toCoordinateForm.longitudeMinutes.required")),
        entry("toCoordinateForm.longitudeSeconds", Set.of("toCoordinateForm.longitudeSeconds.required")),
        entry("toCoordinateForm.longitudeDirection", Set.of("toCoordinateForm.longitudeDirection.required")),
        entry("pipelineType", Set.of("pipelineType.required")),
        entry("length", Set.of("length.required")),
        entry("componentPartsDescription", Set.of("componentPartsDescription.required")),
        entry("productsToBeConveyed", Set.of("productsToBeConveyed.required")),
        entry("trenchedBuriedBackfilled", Set.of("trenchedBuriedBackfilled.required")),
        entry("pipelineFlexibility", Set.of("pipelineFlexibility.required")),
        entry("pipelineMaterial", Set.of("pipelineMaterial.required")),
        entry("pipelineDesignLife", Set.of("pipelineDesignLife.required")),
        entry("pipelineInBundle", Set.of("pipelineInBundle.required"))
    );
  }

  @Test
  public void fromLocation_tooLong() {

    var form = buildForm();
    form.setFromLocation(StringUtils.repeat("a", 201));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(result).containsOnly(entry("fromLocation", Set.of("fromLocation.maxLengthExceeded")));

  }

  @Test
  public void fromLocation_charBoundary() {

    var form = buildForm();
    form.setFromLocation(StringUtils.repeat("a", 200));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(result).isEmpty();

  }

  @Test
  public void fromLocation_unwantedChars() {

    var form = buildForm();
    form.setFromLocation("bad##");

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(result).containsOnly(entry("fromLocation", Set.of("fromLocation.invalid")));

  }

  @Test
  public void toLocation_tooLong() {

    var form = buildForm();
    form.setToLocation(StringUtils.repeat("a", 201));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(result).containsOnly(entry("toLocation", Set.of("toLocation.maxLengthExceeded")));

  }

  @Test
  public void toLocation_charBoundary() {

    var form = buildForm();
    form.setToLocation(StringUtils.repeat("a", 200));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(result).isEmpty();

  }

  @Test
  public void toLocation_unwantedChars() {

    var form = buildForm();
    form.setToLocation("bad##");

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(result).containsOnly(entry("toLocation", Set.of("toLocation.invalid")));

  }

  @Test
  public void failed_length_notPositive() {

    var form = buildForm();

    form.setLength(BigDecimal.valueOf(-1));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(result).containsOnly(entry("length", Set.of("length.invalid")));

  }

  @Test
  public void failed_length_over2Dp() {

    var form = buildForm();

    form.setLength(BigDecimal.valueOf(1.323));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(result).containsOnly(entry("length", Set.of("length.maxDpExceeded")));

  }

  @Test
  public void invalid_productsToBeConveyed_tooLong() {
    var form = buildForm();
    form.setProductsToBeConveyed(ValidatorTestUtils.overMaxDefaultCharLength());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).containsOnly(
        entry("productsToBeConveyed", Set.of("productsToBeConveyed" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

  @Test
  public void invalid_componentPartsDescription_tooLong() {
    var form = buildForm();
    form.setComponentPartsDescription(ValidatorTestUtils.overMaxDefaultCharLength());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).containsOnly(
        entry("componentPartsDescription", Set.of("componentPartsDescription" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

  @Test
  public void failed_trenchingMethodsRequired() {

    var form = buildForm();
    form.setTrenchingMethods(null);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(result).containsOnly(entry("trenchingMethods", Set.of("trenchingMethods.required")));

  }

  @Test
  public void valid_trenchingMethodsNotRequired() {

    var form = buildForm();
    form.setTrenchedBuriedBackfilled(false);
    form.setTrenchingMethods(null);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);

    assertThat(result).isEmpty();

  }

  @Test
  public void invalid_trenchingMethods_tooLong() {
    var form = buildForm();
    form.setTrenchedBuriedBackfilled(true);
    form.setTrenchingMethods(ValidatorTestUtils.overMaxDefaultCharLength());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).containsOnly(
        entry("trenchingMethods", Set.of("trenchingMethods" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

  @Test
  public void valid_otherMaterialUsed() {
    var form = buildForm();
    form.setPipelineMaterial(PipelineMaterial.OTHER);
    form.setOtherPipelineMaterialUsed("other material");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).doesNotContain(entry("otherPipelineMaterialUsed", Set.of("otherPipelineMaterialUsed.required")));
  }

  @Test
  public void invalid_otherMaterialUsed() {
    var form = buildForm();
    form.setPipelineMaterial(PipelineMaterial.OTHER);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).containsOnly(entry("otherPipelineMaterialUsed", Set.of("otherPipelineMaterialUsed.required")));
  }

  @Test
  public void invalid_otherMaterialUsed_tooLong() {
    var form = buildForm();
    form.setPipelineMaterial(PipelineMaterial.OTHER);
    form.setOtherPipelineMaterialUsed(ValidatorTestUtils.overMaxDefaultCharLength());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).containsOnly(
        entry("otherPipelineMaterialUsed", Set.of("otherPipelineMaterialUsed" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

  @Test
  public void invalid_designLife() {
    var form = buildForm();
    form.setPipelineDesignLife(0);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).containsOnly(entry("pipelineDesignLife", Set.of("pipelineDesignLife.invalid")));
  }

  @Test
  public void invalid_bundleName_empty() {
    var form = buildForm();
    form.setPipelineInBundle(true);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).containsOnly(
        entry("bundleName", Set.of("bundleName" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void invalid_bundleName_tooLong() {
    var form = buildForm();
    form.setPipelineInBundle(true);
    form.setBundleName(ValidatorTestUtils.overMaxDefaultCharLength());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).containsOnly(
        entry("bundleName", Set.of("bundleName" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

  @Test
  public void failed_whyNotReturnedToShoreRequired() {
    validationHints = new PipelineHeaderValidationHints(
        PipelineStatus.OUT_OF_USE_ON_SEABED, DO_NOT_VALIDATE_SEABED_QUESTION);
    var form = buildForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).contains(
        entry("whyNotReturnedToShore", Set.of("whyNotReturnedToShore" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void invalid_whyNotReturnedToShore_tooLong() {
    validationHints = new PipelineHeaderValidationHints(
        PipelineStatus.OUT_OF_USE_ON_SEABED, DO_NOT_VALIDATE_SEABED_QUESTION);
    var form = buildForm();
    form.setWhyNotReturnedToShore(ValidatorTestUtils.overMaxDefaultCharLength());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).containsOnly(
        entry("whyNotReturnedToShore", Set.of("whyNotReturnedToShore" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

  @Test
  public void seabedQuestionRequired_null() {
    validationHints = new PipelineHeaderValidationHints(
        PipelineStatus.IN_SERVICE, VALIDATE_SEABED_QUESTION);
    var form = buildForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).contains(
        entry("alreadyExistsOnSeabed", Set.of("alreadyExistsOnSeabed" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void pipelineInUseQuestionRequired_null() {
    validationHints = new PipelineHeaderValidationHints(
        PipelineStatus.IN_SERVICE, VALIDATE_SEABED_QUESTION);
    var form = buildForm();
    form.setAlreadyExistsOnSeabed(true);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).contains(
        entry("pipelineInUse", Set.of("pipelineInUse" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_footnote_lengthExceeded() {
    var form = buildForm();
    form.setFootnote(ValidatorTestUtils.overMaxDefaultCharLength());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHints);
    assertThat(result).contains(
        entry("footnote", Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("footnote"))));
  }


  private PipelineHeaderForm buildForm() {

    var form = new PipelineHeaderForm();

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

    form.setLength(BigDecimal.valueOf(65.66));
    form.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    form.setComponentPartsDescription("component parts");
    form.setProductsToBeConveyed("products");
    form.setTrenchedBuriedBackfilled(true);
    form.setTrenchingMethods("trench methods");

    form.setPipelineFlexibility(PipelineFlexibility.FLEXIBLE);
    form.setPipelineMaterial(PipelineMaterial.CARBON_STEEL);
    form.setPipelineDesignLife(5);
    form.setPipelineInBundle(false);

    return form;

  }

}
