package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.util.Map;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.Chemical;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionDataForm;

public class FluidCompositionView {

  private final Map<Chemical, FluidCompositionDataForm> chemicalDataFormMap;

  public FluidCompositionView(Map<Chemical, FluidCompositionDataForm> chemicalDataFormMap) {
    this.chemicalDataFormMap = chemicalDataFormMap;
  }

  public Map<Chemical, FluidCompositionDataForm> getChemicalDataFormMap() {
    return chemicalDataFormMap;
  }

}
