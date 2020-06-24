package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo;


import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.Chemical;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.FluidCompositionOption;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity
@Table(name = "pad_fluid_composition_info")
public class PadFluidCompositionInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_detail_id")
  @ManyToOne
  private PwaApplicationDetail pwaApplicationDetail;

  @Enumerated(EnumType.STRING)
  private Chemical chemicalName;
  @Enumerated(EnumType.STRING)
  private FluidCompositionOption fluidCompositionOption;
  private double moleValue;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public Chemical getChemicalName() {
    return chemicalName;
  }

  public void setChemicalName(Chemical chemicalName) {
    this.chemicalName = chemicalName;
  }

  public FluidCompositionOption getFluidCompositionOption() {
    return fluidCompositionOption;
  }

  public void setFluidCompositionOption(
      FluidCompositionOption fluidCompositionOption) {
    this.fluidCompositionOption = fluidCompositionOption;
  }

  public double getMoleValue() {
    return moleValue;
  }

  public void setMoleValue(double moleValue) {
    this.moleValue = moleValue;
  }
}
