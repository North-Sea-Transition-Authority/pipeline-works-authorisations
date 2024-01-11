package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.Chemical;

public class FluidCompositionForm {

  private Map<Chemical, FluidCompositionDataForm> chemicalDataFormMap = new HashMap<>();

  private String otherInformation;

  public Map<Chemical, FluidCompositionDataForm> getChemicalDataFormMap() {
    return chemicalDataFormMap;
  }

  public void setChemicalDataFormMap(Map<Chemical, FluidCompositionDataForm> chemicalDataFormMap) {
    this.chemicalDataFormMap = chemicalDataFormMap;
  }

  public String getOtherInformation() {
    return otherInformation;
  }

  public FluidCompositionForm setOtherInformation(String otherInformation) {
    this.otherInformation = otherInformation;
    return this;
  }

  public void addChemicalData(Chemical chemical, FluidCompositionDataForm fluidCompositionDataForm) {
    chemicalDataFormMap.put(chemical, fluidCompositionDataForm);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FluidCompositionForm that = (FluidCompositionForm) o;
    return Objects.equals(chemicalDataFormMap, that.chemicalDataFormMap) && Objects.equals(otherInformation, that.otherInformation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(chemicalDataFormMap, otherInformation);
  }

}
