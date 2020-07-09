package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "pad_pipeline_ident_data")
public class PadPipelineIdentData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "ppi_id")
  private PadPipelineIdent padPipelineIdent;

  @Column(name = "component_parts_desc")
  private String componentPartsDescription;

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

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PadPipelineIdent getPadPipelineIdent() {
    return padPipelineIdent;
  }

  public void setPadPipelineIdent(
      PadPipelineIdent padPipelineIdent) {
    this.padPipelineIdent = padPipelineIdent;
  }

  public String getComponentPartsDescription() {
    return componentPartsDescription;
  }

  public void setComponentPartsDescription(String componentPartsDescription) {
    this.componentPartsDescription = componentPartsDescription;
  }

  public BigDecimal getExternalDiameter() {
    return externalDiameter;
  }

  public void setExternalDiameter(BigDecimal externalDiameter) {
    this.externalDiameter = externalDiameter;
  }

  public BigDecimal getInternalDiameter() {
    return internalDiameter;
  }

  public void setInternalDiameter(BigDecimal internalDiameter) {
    this.internalDiameter = internalDiameter;
  }

  public BigDecimal getWallThickness() {
    return wallThickness;
  }

  public void setWallThickness(BigDecimal wallThickness) {
    this.wallThickness = wallThickness;
  }

  public String getInsulationCoatingType() {
    return insulationCoatingType;
  }

  public void setInsulationCoatingType(String insulationCoatingType) {
    this.insulationCoatingType = insulationCoatingType;
  }

  public BigDecimal getMaop() {
    return maop;
  }

  public void setMaop(BigDecimal maop) {
    this.maop = maop;
  }

  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }


  public String getExternalDiameterMultiCore() {
    return externalDiameterMultiCore;
  }

  public void setExternalDiameterMultiCore(String externalDiameterMultiCore) {
    this.externalDiameterMultiCore = externalDiameterMultiCore;
  }

  public String getInternalDiameterMultiCore() {
    return internalDiameterMultiCore;
  }

  public void setInternalDiameterMultiCore(String internalDiameterMultiCore) {
    this.internalDiameterMultiCore = internalDiameterMultiCore;
  }

  public String getWallThicknessMultiCore() {
    return wallThicknessMultiCore;
  }

  public void setWallThicknessMultiCore(String wallThicknessMultiCore) {
    this.wallThicknessMultiCore = wallThicknessMultiCore;
  }

  public String getInsulationCoatingTypeMultiCore() {
    return insulationCoatingTypeMultiCore;
  }

  public void setInsulationCoatingTypeMultiCore(String insulationCoatingTypeMultiCore) {
    this.insulationCoatingTypeMultiCore = insulationCoatingTypeMultiCore;
  }

  public String getMaopMultiCore() {
    return maopMultiCore;
  }

  public void setMaopMultiCore(String maopMultiCore) {
    this.maopMultiCore = maopMultiCore;
  }

  public String getProductsToBeConveyedMultiCore() {
    return productsToBeConveyedMultiCore;
  }

  public void setProductsToBeConveyedMultiCore(String productsToBeConveyedMultiCore) {
    this.productsToBeConveyedMultiCore = productsToBeConveyedMultiCore;
  }
}
