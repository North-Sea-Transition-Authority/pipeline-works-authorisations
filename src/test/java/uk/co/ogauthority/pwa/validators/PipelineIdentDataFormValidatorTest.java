package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentDataFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentDataValidationRule;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PipelineIdentDataFormValidatorTest {

  private PipelineIdentDataFormValidator validator;

  @Before
  public void setUp() {
    validator = new PipelineIdentDataFormValidator();
  }

  @Test
  public void valid_singleCore_mandatory_dataPresent() {

    var form = buildForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE, PipelineIdentDataValidationRule.AS_SECTION);

    assertThat(result).isEmpty();

  }

  @Test
  public void valid_multiCore__mandatory_dataPresent() {

    var form = new PipelineIdentDataForm();
    form.setComponentPartsDescription("text");
    form.setExternalDiameterMultiCore("text");
    form.setInternalDiameterMultiCore("text");
    form.setWallThicknessMultiCore("text");
    form.setMaopMultiCore("text");
    form.setInsulationCoatingTypeMultiCore("text");
    form.setProductsToBeConveyedMultiCore("text");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.MULTI_CORE, PipelineIdentDataValidationRule.AS_SECTION);

    assertThat(result).isEmpty();

  }

  @Test
  public void failed_multiCore__mandatory_dataTooBig() {

    var form = new PipelineIdentDataForm();
    form.setComponentPartsDescription(ValidatorTestUtils.over4000Chars());
    form.setExternalDiameterMultiCore(ValidatorTestUtils.over4000Chars());
    form.setInternalDiameterMultiCore(ValidatorTestUtils.over4000Chars());
    form.setWallThicknessMultiCore(ValidatorTestUtils.over4000Chars());
    form.setMaopMultiCore(ValidatorTestUtils.over4000Chars());
    form.setInsulationCoatingTypeMultiCore(ValidatorTestUtils.over4000Chars());
    form.setProductsToBeConveyedMultiCore(ValidatorTestUtils.over4000Chars());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.MULTI_CORE, PipelineIdentDataValidationRule.AS_SECTION);

    assertThat(result).containsOnly(
        entry("componentPartsDescription", Set.of("componentPartsDescription.maxLengthExceeded")),
        entry("externalDiameterMultiCore", Set.of("externalDiameterMultiCore.maxLengthExceeded")),
        entry("internalDiameterMultiCore", Set.of("internalDiameterMultiCore.maxLengthExceeded")),
        entry("wallThicknessMultiCore", Set.of("wallThicknessMultiCore.maxLengthExceeded")),
        entry("maopMultiCore", Set.of("maopMultiCore.maxLengthExceeded")),
        entry("insulationCoatingTypeMultiCore", Set.of("insulationCoatingTypeMultiCore.maxLengthExceeded")),
        entry("productsToBeConveyedMultiCore", Set.of("productsToBeConveyedMultiCore.maxLengthExceeded"))
    );

  }

  @Test
  public void failed_mandatory_dataNotPresent() {

    var form = new PipelineIdentDataForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE, PipelineIdentDataValidationRule.AS_SECTION);

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

  @Test
  public void failed_multiCore_definingStructure_mandatory_dataNotPresent() {

    var form = new PipelineIdentDataForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.MULTI_CORE, PipelineIdentDataValidationRule.AS_STRUCTURE);

    assertThat(result).containsOnly(
        entry("componentPartsDescription", Set.of("componentPartsDescription.required")),
        entry("productsToBeConveyedMultiCore", Set.of("productsToBeConveyedMultiCore.required"))
    );

  }

  @Test
  public void failed_multiCore_notDefiningStructure_mandatory_dataNotPresent() {

    var form = new PipelineIdentDataForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.MULTI_CORE, PipelineIdentDataValidationRule.AS_SECTION);

    assertThat(result).containsOnly(
        entry("componentPartsDescription", Set.of("componentPartsDescription.required")),
        entry("externalDiameterMultiCore", Set.of("externalDiameterMultiCore.required")),
        entry("internalDiameterMultiCore", Set.of("internalDiameterMultiCore.required")),
        entry("wallThicknessMultiCore", Set.of("wallThicknessMultiCore.required")),
        entry("maopMultiCore", Set.of("maopMultiCore.required")),
        entry("insulationCoatingTypeMultiCore", Set.of("insulationCoatingTypeMultiCore.required")),
        entry("productsToBeConveyedMultiCore", Set.of("productsToBeConveyedMultiCore.required"))
    );

  }

  @Test
  public void failed_singleCore_definingStructure_mandatory_dataNotPresent() {

    var form = new PipelineIdentDataForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE, PipelineIdentDataValidationRule.AS_STRUCTURE);

    assertThat(result).containsOnly(
        entry("componentPartsDescription", Set.of("componentPartsDescription.required")),
        entry("productsToBeConveyed", Set.of("productsToBeConveyed.required"))
    );

  }

  @Test
  public void failed_singleCore_notDefiningStructure_mandatory_dataNotPresent() {

    var form = new PipelineIdentDataForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, (Object) null, PipelineCoreType.SINGLE_CORE, PipelineIdentDataValidationRule.AS_SECTION);

    assertThat(result).containsOnly(
        entry("componentPartsDescription", Set.of("componentPartsDescription.required")),
        entry("externalDiameter", Set.of("externalDiameter.required")),
        entry("internalDiameter", Set.of("internalDiameter.required")),
        entry("wallThickness", Set.of("wallThickness.required")),
        entry("maop", Set.of("maop.required")),
        entry("insulationCoatingType", Set.of("insulationCoatingType.required")),
        entry("productsToBeConveyed", Set.of("productsToBeConveyed.required"))
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
