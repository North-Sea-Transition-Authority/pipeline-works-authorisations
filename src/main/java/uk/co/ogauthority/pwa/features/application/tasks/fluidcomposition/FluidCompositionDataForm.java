package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;


import java.util.Objects;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;

public class FluidCompositionDataForm {

  private ChemicalMeasurementType chemicalMeasurementType;
  private DecimalInput measurementValue;


  public ChemicalMeasurementType getChemicalMeasurementType() {
    return chemicalMeasurementType;
  }

  public void setChemicalMeasurementType(
      ChemicalMeasurementType chemicalMeasurementType) {
    this.chemicalMeasurementType = chemicalMeasurementType;
  }

  public DecimalInput getMeasurementValue() {
    return measurementValue;
  }

  public void setMeasurementValue(DecimalInput measurementValue) {
    this.measurementValue = measurementValue;
  }


  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FluidCompositionDataForm that = (FluidCompositionDataForm) o;
    return chemicalMeasurementType == that.chemicalMeasurementType
        && Objects.equals(measurementValue, that.measurementValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(chemicalMeasurementType, measurementValue);
  }
}
