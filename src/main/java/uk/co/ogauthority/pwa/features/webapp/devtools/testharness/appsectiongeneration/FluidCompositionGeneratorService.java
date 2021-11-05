package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.Chemical;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.FluidCompositionDataForm;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.FluidCompositionForm;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.FluidCompositionOption;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.PadFluidCompositionInfoService;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;

@Service
@Profile("test-harness")
class FluidCompositionGeneratorService implements TestHarnessAppFormService {

  private final PadFluidCompositionInfoService padFluidCompositionService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.FLUID_COMPOSITION;


  @Autowired
  public FluidCompositionGeneratorService(
      PadFluidCompositionInfoService padFluidCompositionService) {
    this.padFluidCompositionService = padFluidCompositionService;
  }

  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var form = createForm();
    var entities = padFluidCompositionService.getPadFluidCompositionInfoEntities(appFormServiceParams.getApplicationDetail());
    padFluidCompositionService.saveEntitiesUsingForm(form, entities);
  }


  private FluidCompositionForm createForm() {

    Map<Chemical, FluidCompositionDataForm> chemicalDataFormMap = new EnumMap<>(Chemical.class);
    Chemical.asList().forEach(chemical -> {
      var dataForm = new FluidCompositionDataForm();
      dataForm.setFluidCompositionOption(FluidCompositionOption.NONE);
      chemicalDataFormMap.put(chemical, dataForm);
    });

    //required to meet validation rule
    var firstFluidComposition = chemicalDataFormMap.values().stream().findFirst()
        .orElseThrow(() -> new ValueNotFoundException("No FluidCompositionDataForm found"));
    firstFluidComposition.setFluidCompositionOption(FluidCompositionOption.HIGHER_AMOUNT);
    firstFluidComposition.setMoleValue(new DecimalInput(BigDecimal.valueOf(99)));

    var form = new FluidCompositionForm();
    form.setChemicalDataFormMap(chemicalDataFormMap);
    return form;
  }

}
