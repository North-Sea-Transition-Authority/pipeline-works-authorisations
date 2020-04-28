package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineHeaderFormValidator;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

public class PipelineHeaderFormValidatorTest {

  private PipelineHeaderFormValidator validator;

  @Before
  public void setUp() {
    validator = new PipelineHeaderFormValidator();
  }

  @Test
  public void valid() {
    var result = ValidatorTestUtils.getFormValidationErrors(validator, buildForm(), (Object) null);
    assertThat(result).isEmpty();
  }

  @Test
  public void failed_mandatory() {
    var form = new PipelineHeaderForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null);

    assertThat(result).contains(
        entry("fromLocation", Set.of("fromLocation.required")),
        entry("fromLatDeg", Set.of("fromLatDeg.required")),
        entry("fromLatMin", Set.of("fromLatMin.required")),
        entry("fromLatSec", Set.of("fromLatSec.required")),
        entry("fromLongDeg", Set.of("fromLongDeg.required")),
        entry("fromLongMin", Set.of("fromLongMin.required")),
        entry("fromLongSec", Set.of("fromLongSec.required")),
        entry("fromLongDirection", Set.of("fromLongDirection.required")),
        entry("toLatDeg", Set.of("toLatDeg.required")),
        entry("toLatMin", Set.of("toLatMin.required")),
        entry("toLatSec", Set.of("toLatSec.required")),
        entry("toLongDeg", Set.of("toLongDeg.required")),
        entry("toLongMin", Set.of("toLongMin.required")),
        entry("toLongSec", Set.of("toLongSec.required")),
        entry("toLongDirection", Set.of("toLongDirection.required")),
        entry("pipelineType", Set.of("pipelineType.required")),
        entry("length", Set.of("length.required")),
        entry("componentPartsDescription", Set.of("componentPartsDescription.required")),
        entry("productsToBeConveyed", Set.of("productsToBeConveyed.required")),
        entry("trenchedBuriedBackfilled", Set.of("trenchedBuriedBackfilled.required"))
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

  private PipelineHeaderForm buildForm() {

    var form = new PipelineHeaderForm();

    form.setFromLocation("from");
    form.setFromLatDeg(55);
    form.setFromLatMin(30);
    form.setFromLatSec(BigDecimal.valueOf(22.22));
    form.setFromLongDeg(13);
    form.setFromLongMin(22);
    form.setFromLongSec(BigDecimal.valueOf(12.1));
    form.setFromLongDirection(LongitudeDirection.EAST);
    form.setToLocation("to");
    form.setToLatDeg(54);
    form.setToLatMin(22);
    form.setToLatSec(BigDecimal.valueOf(25));
    form.setToLongDeg(22);
    form.setToLongMin(21);
    form.setToLongSec(BigDecimal.valueOf(1));
    form.setToLongDirection(LongitudeDirection.WEST);

    form.setLength(BigDecimal.valueOf(65.66));
    form.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    form.setComponentPartsDescription("component parts");
    form.setProductsToBeConveyed("products");
    form.setTrenchedBuriedBackfilled(true);
    form.setTrenchingMethods("trench methods");

    return form;

  }

}
