package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentDataFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentFormValidator;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PipelineIdentFormValidatorTest {

  private PipelineIdentFormValidator validator;

  @Before
  public void setUp() {
    validator = new PipelineIdentFormValidator(new PipelineIdentDataFormValidator(), new CoordinateFormValidator());
  }

  @Test
  public void valid() {
    var result = ValidatorTestUtils.getFormValidationErrors(validator, buildForm(), (Object) null);
    assertThat(result).isEmpty();
  }

  @Test
  public void failed_mandatory() {
    var form = new PipelineIdentForm();
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    form.setDataForm(new PipelineIdentDataForm());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null);

    assertThat(result).contains(
        entry("fromLocation", Set.of("fromLocation.required")),
        entry("toLocation", Set.of("toLocation.required")),
        entry("length", Set.of("length.required")),
        entry("dataForm.componentPartsDescription", Set.of("componentPartsDescription.required")),
        entry("dataForm.productsToBeConveyed", Set.of("productsToBeConveyed.required")),
        entry("dataForm.maop", Set.of("maop.required")),
        entry("dataForm.externalDiameter", Set.of("externalDiameter.required")),
        entry("dataForm.internalDiameter", Set.of("internalDiameter.required")),
        entry("dataForm.wallThickness", Set.of("wallThickness.required")),
        entry("dataForm.insulationCoatingType", Set.of("insulationCoatingType.required"))
    );
  }

  private PipelineIdentForm buildForm() {

    var form = new PipelineIdentForm();

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

    form.setLength(BigDecimal.valueOf(65.5));

    var dataForm = new PipelineIdentDataForm();
    dataForm.setExternalDiameter(BigDecimal.valueOf(12.1));
    dataForm.setInternalDiameter(BigDecimal.valueOf(12.1));
    dataForm.setWallThickness(BigDecimal.valueOf(12.1));
    dataForm.setMaop(BigDecimal.valueOf(12.1));
    dataForm.setProductsToBeConveyed("prod");
    dataForm.setComponentPartsDescription("component");
    dataForm.setInsulationCoatingType("ins");
    form.setDataForm(dataForm);

    return form;

  }

}
