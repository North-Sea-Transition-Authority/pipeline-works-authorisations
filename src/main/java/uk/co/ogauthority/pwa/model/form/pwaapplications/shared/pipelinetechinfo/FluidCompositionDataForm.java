package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo;


import java.math.BigDecimal;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.FluidCompositionOption;

public class FluidCompositionDataForm {

  private FluidCompositionOption fluidCompositionOption;
  private BigDecimal moleValue;


  public FluidCompositionOption getFluidCompositionOption() {
    return fluidCompositionOption;
  }

  public void setFluidCompositionOption(
      FluidCompositionOption fluidCompositionOption) {
    this.fluidCompositionOption = fluidCompositionOption;
  }

  public BigDecimal getMoleValue() {
    return moleValue;
  }

  public void setMoleValue(BigDecimal moleValue) {
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
