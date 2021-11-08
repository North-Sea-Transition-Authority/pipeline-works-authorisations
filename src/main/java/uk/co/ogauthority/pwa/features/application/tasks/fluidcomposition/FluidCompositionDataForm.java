package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;


import java.util.Objects;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;

public class FluidCompositionDataForm {

  private FluidCompositionOption fluidCompositionOption;
  private DecimalInput moleValue;


  public FluidCompositionOption getFluidCompositionOption() {
    return fluidCompositionOption;
  }

  public void setFluidCompositionOption(
      FluidCompositionOption fluidCompositionOption) {
    this.fluidCompositionOption = fluidCompositionOption;
  }

  public DecimalInput getMoleValue() {
    return moleValue;
  }

  public void setMoleValue(DecimalInput moleValue) {
    this.moleValue = moleValue;
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
    return fluidCompositionOption == that.fluidCompositionOption
        && Objects.equals(moleValue, that.moleValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fluidCompositionOption, moleValue);
  }
}
