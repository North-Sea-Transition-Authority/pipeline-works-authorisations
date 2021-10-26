package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import java.util.Map;

public class FluidCompositionView {

  private final Map<Chemical, FluidCompositionDataForm> chemicalDataFormMap;

  public FluidCompositionView(Map<Chemical, FluidCompositionDataForm> chemicalDataFormMap) {
    this.chemicalDataFormMap = chemicalDataFormMap;
  }

  public Map<Chemical, FluidCompositionDataForm> getChemicalDataFormMap() {
    return chemicalDataFormMap;
  }

}
