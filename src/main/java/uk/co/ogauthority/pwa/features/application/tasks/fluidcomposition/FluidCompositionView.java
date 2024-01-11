package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import java.util.Map;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.Chemical;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class FluidCompositionView {

  private final Map<Chemical, FluidCompositionDataForm> chemicalDataFormMap;

  private final String otherDescription;

  public FluidCompositionView(Map<Chemical, FluidCompositionDataForm> chemicalDataFormMap, PwaApplicationDetail applicationDetail) {
    this.chemicalDataFormMap = chemicalDataFormMap;
    this.otherDescription = applicationDetail.getOtherFluidDescription();
  }

  public Map<Chemical, FluidCompositionDataForm> getChemicalDataFormMap() {
    return chemicalDataFormMap;
  }

  public String getOtherDescription() {
    return otherDescription;
  }
}
