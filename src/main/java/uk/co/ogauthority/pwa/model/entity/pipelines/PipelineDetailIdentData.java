package uk.co.ogauthority.pwa.model.entity.pipelines;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "pipeline_detail_ident_data")
public class PipelineDetailIdentData {

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


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public PipelineDetailIdent getPipelineDetailIdent() {
    return pipelineDetailIdent;
  }

  public void setPipelineDetailIdent(PipelineDetailIdent pipelineDetailIdent) {
    this.pipelineDetailIdent = pipelineDetailIdent;
  }

  public String getComponentPartsDesc() {
    return componentPartsDesc;
  }

  public void setComponentPartsDesc(String componentPartsDesc) {
    this.componentPartsDesc = componentPartsDesc;
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

}
