package uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents;

import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdent;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentData;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity
@Table(name = "pad_pipeline_ident_data")
public class PadPipelineIdentData implements PipelineIdentData, ChildEntity<Integer, PadPipelineIdent> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "ppi_id")
  private PadPipelineIdent padPipelineIdent;

  @Column(name = "component_parts_desc")
  private String componentPartsDesc;

  private BigDecimal externalDiameter;

  private BigDecimal internalDiameter;

  private BigDecimal wallThickness;

  private String insulationCoatingType;

  private BigDecimal maop;

  private String productsToBeConveyed;

  @Column(name = "external_diameter_mc")
  private String externalDiameterMultiCore;

  @Column(name = "internal_diameter_mc")
  private String internalDiameterMultiCore;

  @Column(name = "wall_thickness_mc")
  private String wallThicknessMultiCore;

  @Column(name = "insulation_coating_type_mc")
  private String insulationCoatingTypeMultiCore;

  @Column(name = "maop_mc")
  private String maopMultiCore;

  @Column(name = "products_to_be_conveyed_mc")
  private String productsToBeConveyedMultiCore;


  public PadPipelineIdentData() {
  }

  public PadPipelineIdentData(PadPipelineIdent padPipelineIdent) {
    this.padPipelineIdent = padPipelineIdent;
  }


  // ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PadPipelineIdent parentEntity) {
    this.padPipelineIdent = parentEntity;
  }

  @Override
  public PadPipelineIdent getParent() {
    return this.padPipelineIdent;
  }


  // PipelineIdentData methods

  @Override
  public Integer getPipelineIdentDataId() {
    return this.id;
  }

  @Override
  public PipelineIdent getPipelineIdent() {
    return this.padPipelineIdent;
  }

  @Override
  public String getComponentPartsDesc() {
    return this.componentPartsDesc;
  }

  @Override
  public BigDecimal getExternalDiameter() {
    return this.externalDiameter;
  }

  @Override
  public BigDecimal getInternalDiameter() {
    return this.internalDiameter;
  }

  @Override
  public BigDecimal getWallThickness() {
    return this.wallThickness;
  }

  @Override
  public String getInsulationCoatingType() {
    return this.insulationCoatingType;
  }

  @Override
  public BigDecimal getMaop() {
    return this.maop;
  }

  @Override
  public String getProductsToBeConveyed() {
    return this.productsToBeConveyed;
  }

  @Override
  public String getExternalDiameterMultiCore() {
    return this.externalDiameterMultiCore;
  }

  @Override
  public String getInternalDiameterMultiCore() {
    return this.internalDiameterMultiCore;
  }

  @Override
  public String getWallThicknessMultiCore() {
    return this.wallThicknessMultiCore;
  }

  @Override
  public String getInsulationCoatingTypeMultiCore() {
    return this.insulationCoatingTypeMultiCore;
  }

  @Override
  public String getMaopMultiCore() {
    return this.maopMultiCore;
  }

  @Override
  public String getProductsToBeConveyedMultiCore() {
    return this.productsToBeConveyedMultiCore;
  }

  // Getters
  public Integer getId() {
    return id;
  }

  public PadPipelineIdent getPadPipelineIdent() {
    return padPipelineIdent;
  }

  // Setters
  public void setId(Integer id) {
    this.id = id;
  }

  public void setPadPipelineIdent(
      PadPipelineIdent padPipelineIdent) {
    this.padPipelineIdent = padPipelineIdent;
  }

  @Override
  public void setComponentPartsDesc(String componentPartsDesc) {
    this.componentPartsDesc = componentPartsDesc;
  }

  @Override
  public void setExternalDiameter(BigDecimal externalDiameter) {
    this.externalDiameter = externalDiameter;
  }

  @Override
  public void setInternalDiameter(BigDecimal internalDiameter) {
    this.internalDiameter = internalDiameter;
  }

  @Override
  public void setWallThickness(BigDecimal wallThickness) {
    this.wallThickness = wallThickness;
  }

  @Override
  public void setInsulationCoatingType(String insulationCoatingType) {
    this.insulationCoatingType = insulationCoatingType;
  }

  @Override
  public void setMaop(BigDecimal maop) {
    this.maop = maop;
  }

  @Override
  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }

  @Override
  public void setExternalDiameterMultiCore(String externalDiameterMultiCore) {
    this.externalDiameterMultiCore = externalDiameterMultiCore;
  }

  @Override
  public void setInternalDiameterMultiCore(String internalDiameterMultiCore) {
    this.internalDiameterMultiCore = internalDiameterMultiCore;
  }

  @Override
  public void setWallThicknessMultiCore(String wallThicknessMultiCore) {
    this.wallThicknessMultiCore = wallThicknessMultiCore;
  }

  @Override
  public void setInsulationCoatingTypeMultiCore(String insulationCoatingTypeMultiCore) {
    this.insulationCoatingTypeMultiCore = insulationCoatingTypeMultiCore;
  }

  @Override
  public void setMaopMultiCore(String maopMultiCore) {
    this.maopMultiCore = maopMultiCore;
  }

  @Override
  public void setProductsToBeConveyedMultiCore(String productsToBeConveyedMultiCore) {
    this.productsToBeConveyedMultiCore = productsToBeConveyedMultiCore;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadPipelineIdentData that = (PadPipelineIdentData) o;
    return Objects.equals(id, that.id) && Objects.equals(padPipelineIdent,
        that.padPipelineIdent) && Objects.equals(componentPartsDesc,
        that.componentPartsDesc) && Objects.equals(externalDiameter,
        that.externalDiameter) && Objects.equals(internalDiameter,
        that.internalDiameter) && Objects.equals(wallThickness, that.wallThickness) && Objects.equals(
        insulationCoatingType, that.insulationCoatingType) && Objects.equals(maop,
        that.maop) && Objects.equals(productsToBeConveyed,
        that.productsToBeConveyed) && Objects.equals(externalDiameterMultiCore,
        that.externalDiameterMultiCore) && Objects.equals(internalDiameterMultiCore,
        that.internalDiameterMultiCore) && Objects.equals(wallThicknessMultiCore,
        that.wallThicknessMultiCore) && Objects.equals(insulationCoatingTypeMultiCore,
        that.insulationCoatingTypeMultiCore) && Objects.equals(maopMultiCore,
        that.maopMultiCore) && Objects.equals(productsToBeConveyedMultiCore,
        that.productsToBeConveyedMultiCore);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, padPipelineIdent, componentPartsDesc, externalDiameter, internalDiameter, wallThickness,
        insulationCoatingType, maop, productsToBeConveyed, externalDiameterMultiCore, internalDiameterMultiCore,
        wallThicknessMultiCore, insulationCoatingTypeMultiCore, maopMultiCore, productsToBeConveyedMultiCore);
  }
}
