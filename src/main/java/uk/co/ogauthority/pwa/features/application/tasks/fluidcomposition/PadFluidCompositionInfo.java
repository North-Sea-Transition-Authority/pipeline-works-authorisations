package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;


@Entity
@Table(name = "pad_fluid_composition_info")
public class PadFluidCompositionInfo implements ChildEntity<Integer, PwaApplicationDetail> {

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
  private BigDecimal moleValue;


  public PadFluidCompositionInfo() {
    // default constructor required by hibernate
  }

  public PadFluidCompositionInfo(
      PwaApplicationDetail pwaApplicationDetail,
      Chemical chemicalName) {
    this.pwaApplicationDetail = pwaApplicationDetail;
    this.chemicalName = chemicalName;
  }

  //ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PwaApplicationDetail parentEntity) {
    this.pwaApplicationDetail = parentEntity;
  }

  @Override
  public PwaApplicationDetail getParent() {
    return this.pwaApplicationDetail;
  }

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
    PadFluidCompositionInfo that = (PadFluidCompositionInfo) o;
    return Objects.equals(moleValue, that.moleValue)
        && Objects.equals(id, that.id)
        && Objects.equals(pwaApplicationDetail, that.pwaApplicationDetail)
        && chemicalName == that.chemicalName
        && fluidCompositionOption == that.fluidCompositionOption;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplicationDetail, chemicalName, fluidCompositionOption, moleValue);
  }


}
