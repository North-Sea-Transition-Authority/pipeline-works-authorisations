package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentDataFormValidator;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PipelineIdentDataFormValidatorTest {

  private PipelineIdentDataFormValidator validator;

  @Before
  public void setUp() {
    validator = new PipelineIdentDataFormValidator();
  }

  @Test
  public void valid_mandatory_dataPresent() {

    var form = buildForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null);

    assertThat(result).isEmpty();

  }

  @Test
  public void failed_mandatory_dataNotPresent() {

    var form = new PipelineIdentDataForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null);

    assertThat(result).containsOnly(
        entry("componentPartsDescription", Set.of("componentPartsDescription.required")),
        entry("productsToBeConveyed", Set.of("productsToBeConveyed.required")),
        entry("maop", Set.of("maop.required")),
        entry("externalDiameter", Set.of("externalDiameter.required")),
        entry("internalDiameter", Set.of("internalDiameter.required")),
        entry("wallThickness", Set.of("wallThickness.required")),
        entry("insulationCoatingType", Set.of("insulationCoatingType.required"))
    );

  }

  private PipelineIdentDataForm buildForm() {

    var form = new PipelineIdentDataForm();

    form.setExternalDiameter(BigDecimal.valueOf(10.1));
    form.setInternalDiameter(BigDecimal.valueOf(11.12));
    form.setWallThickness(BigDecimal.valueOf(12));
    form.setMaop(BigDecimal.valueOf(400));
    form.setComponentPartsDescription("comp");
    form.setProductsToBeConveyed("prod");
    form.setInsulationCoatingType("insu");

    return form;

  }


}
