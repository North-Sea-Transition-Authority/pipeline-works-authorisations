package uk.co.ogauthority.pwa.model.entity.pipelines;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "pipeline_detail_ident_data")
public class PipelineDetailIdentData implements PipelineIdentData {

  @Id
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pipeline_detail_ident_id")
  private PipelineDetailIdent pipelineDetailIdent;

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


  // Interface implementations

  @Override
  public Integer getPipelineIdentDataId() {
    return this.id;
  }

  @Override
  public PipelineIdent getPipelineIdent() {
    return this.pipelineDetailIdent;
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

  public PipelineDetailIdent getPipelineDetailIdent() {
    return pipelineDetailIdent;
  }

  // Setters
  public void setId(Integer id) {
    this.id = id;
  }

  public void setPipelineDetailIdent(PipelineDetailIdent pipelineDetailIdent) {
    this.pipelineDetailIdent = pipelineDetailIdent;
  }

  public void setComponentPartsDesc(String componentPartsDesc) {
    this.componentPartsDesc = componentPartsDesc;
  }

  public void setExternalDiameter(BigDecimal externalDiameter) {
    this.externalDiameter = externalDiameter;
  }

  public void setInternalDiameter(BigDecimal internalDiameter) {
    this.internalDiameter = internalDiameter;
  }

  public void setWallThickness(BigDecimal wallThickness) {
    this.wallThickness = wallThickness;
  }

  public void setInsulationCoatingType(String insulationCoatingType) {
    this.insulationCoatingType = insulationCoatingType;
  }

  public void setMaop(BigDecimal maop) {
    this.maop = maop;
  }

  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }

  public void setExternalDiameterMultiCore(String externalDiameterMultiCore) {
    this.externalDiameterMultiCore = externalDiameterMultiCore;
  }

  public void setInternalDiameterMultiCore(String internalDiameterMultiCore) {
    this.internalDiameterMultiCore = internalDiameterMultiCore;
  }

  public void setWallThicknessMultiCore(String wallThicknessMultiCore) {
    this.wallThicknessMultiCore = wallThicknessMultiCore;
  }

  public void setInsulationCoatingTypeMultiCore(String insulationCoatingTypeMultiCore) {
    this.insulationCoatingTypeMultiCore = insulationCoatingTypeMultiCore;
  }

  public void setMaopMultiCore(String maopMultiCore) {
    this.maopMultiCore = maopMultiCore;
  }

  public void setProductsToBeConveyedMultiCore(String productsToBeConveyedMultiCore) {
    this.productsToBeConveyedMultiCore = productsToBeConveyedMultiCore;
  }
}
