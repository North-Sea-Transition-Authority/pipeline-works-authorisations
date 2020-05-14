package uk.co.ogauthority.pwa.model.entity.pipelines;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pipeline_detail_ident_data")
public class PipelineDetailIdentData {

  @Id
  private Integer id;
  private String pipelineDetailIdentId;
  private String componentPartsDesc;
  private String externalDiameter;
  private String internalDiameter;
  private String wallThickness;
  private String insulationCoatingType;
  private String maop;
  private String productsToBeConveyed;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public String getPipelineDetailIdentId() {
    return pipelineDetailIdentId;
  }

  public void setPipelineDetailIdentId(String pipelineDetailIdentId) {
    this.pipelineDetailIdentId = pipelineDetailIdentId;
  }


  public String getComponentPartsDesc() {
    return componentPartsDesc;
  }

  public void setComponentPartsDesc(String componentPartsDesc) {
    this.componentPartsDesc = componentPartsDesc;
  }


  public String getExternalDiameter() {
    return externalDiameter;
  }

  public void setExternalDiameter(String externalDiameter) {
    this.externalDiameter = externalDiameter;
  }


  public String getInternalDiameter() {
    return internalDiameter;
  }

  public void setInternalDiameter(String internalDiameter) {
    this.internalDiameter = internalDiameter;
  }


  public String getWallThickness() {
    return wallThickness;
  }

  public void setWallThickness(String wallThickness) {
    this.wallThickness = wallThickness;
  }


  public String getInsulationCoatingType() {
    return insulationCoatingType;
  }

  public void setInsulationCoatingType(String insulationCoatingType) {
    this.insulationCoatingType = insulationCoatingType;
  }


  public String getMaop() {
    return maop;
  }

  public void setMaop(String maop) {
    this.maop = maop;
  }


  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }

}
