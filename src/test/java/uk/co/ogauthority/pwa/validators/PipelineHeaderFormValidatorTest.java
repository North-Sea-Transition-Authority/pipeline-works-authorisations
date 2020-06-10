package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineFlexibility;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineHeaderFormValidator;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

public class PipelineHeaderFormValidatorTest {

  private PipelineHeaderFormValidator validator;

  @Before
  public void setUp() {
    validator = new PipelineHeaderFormValidator(new CoordinateFormValidator());
  }

  @Test
  public void valid() {
    var result = ValidatorTestUtils.getFormValidationErrors(validator, buildForm(), (Object) null);
    assertThat(result).isEmpty();
  }

  @Test
  public void failed_mandatory() {
    var form = new PipelineHeaderForm();
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null);

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
        entry("pipelineFlexibility", Set.of("pipelineFlexibility.required")),
        entry("pipelineMaterial", Set.of("pipelineMaterial.required")),
        entry("pipelineDesignLife", Set.of("pipelineDesignLife.required"))
    );
  }

  @Test
  public void failed_trenchingMethodsRequired() {

    var form = buildForm();
    form.setTrenchingMethods(null);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null);

    assertThat(result).containsOnly(entry("trenchingMethods", Set.of("trenchingMethods.required")));

  }

  @Test
  public void valid_trenchingMethodsNotRequired() {

    var form = buildForm();
    form.setTrenchedBuriedBackfilled(false);
    form.setTrenchingMethods(null);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null);

    assertThat(result).isEmpty();

  }

  @Test
  public void invalid_otherMaterialUsed() {
    var form = buildForm();
    form.setPipelineMaterial(PipelineMaterial.OTHER);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null);
    assertThat(result).containsOnly(entry("otherPipelineMaterialUsed", Set.of("otherPipelineMaterialUsed.required")));
  }

  @Test
  public void invalid_designLife() {
    var form = buildForm();
    form.setPipelineDesignLife(0);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null);
    assertThat(result).containsOnly(entry("pipelineDesignLife", Set.of("pipelineDesignLife.invalid")));
  }


  private PipelineHeaderForm buildForm() {

    var form = new PipelineHeaderForm();

    form.setFromLocation("from");
    var fromCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(55, 55, BigDecimal.valueOf(55.55), LatitudeDirection.NORTH),
            new LongitudeCoordinate(12, 12, BigDecimal.valueOf(12), LongitudeDirection.EAST)
        ), fromCoordinateForm
    );
    form.setFromCoordinateForm(fromCoordinateForm);

    form.setToLocation("to");
    var toCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(46, 46, BigDecimal.valueOf(46), LatitudeDirection.SOUTH),
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


    return form;

  }

}
