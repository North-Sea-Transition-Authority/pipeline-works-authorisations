package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineFlexibility;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;

public class PipelineHeaderForm {

  private String fromLocation;

  private CoordinateForm fromCoordinateForm;

  private String toLocation;

  private CoordinateForm toCoordinateForm;

  private PipelineType pipelineType;

  private String componentPartsDescription;

  private BigDecimal length;

  private String productsToBeConveyed;

  private Boolean trenchedBuriedBackfilled;
  private String trenchingMethods;

  private PipelineFlexibility pipelineFlexibility;

  private PipelineMaterial pipelineMaterial;
  private String otherPipelineMaterialUsed;

  private Integer pipelineDesignLife;

  private Boolean pipelineInBundle;
  private String bundleName;

  private String whyNotReturnedToShore;

  private Boolean alreadyExistsOnSeabed;

  private Boolean pipelineInUse;

  private String footnote;



  public String getFromLocation() {
    return fromLocation;
  }

  public void setFromLocation(String fromLocation) {
    this.fromLocation = fromLocation;
  }

  public CoordinateForm getFromCoordinateForm() {
    return fromCoordinateForm;
  }

  public void setFromCoordinateForm(CoordinateForm fromCoordinateForm) {
    this.fromCoordinateForm = fromCoordinateForm;
  }

  public String getToLocation() {
    return toLocation;
  }

  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
  }

  public CoordinateForm getToCoordinateForm() {
    return toCoordinateForm;
  }

  public void setToCoordinateForm(CoordinateForm toCoordinateForm) {
    this.toCoordinateForm = toCoordinateForm;
  }

  public PipelineType getPipelineType() {
    return pipelineType;
  }

  public void setPipelineType(PipelineType pipelineType) {
    this.pipelineType = pipelineType;
  }

  public String getComponentPartsDescription() {
    return componentPartsDescription;
  }

  public void setComponentPartsDescription(String componentPartsDescription) {
    this.componentPartsDescription = componentPartsDescription;
  }

  public BigDecimal getLength() {
    return length;
  }

  public void setLength(BigDecimal length) {
    this.length = length;
  }

  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }

  public Boolean getTrenchedBuriedBackfilled() {
    return trenchedBuriedBackfilled;
  }

  public void setTrenchedBuriedBackfilled(Boolean trenchedBuriedBackfilled) {
    this.trenchedBuriedBackfilled = trenchedBuriedBackfilled;
  }

  public String getTrenchingMethods() {
    return trenchingMethods;
  }

  public void setTrenchingMethods(String trenchingMethods) {
    this.trenchingMethods = trenchingMethods;
  }

  public PipelineFlexibility getPipelineFlexibility() {
    return pipelineFlexibility;
  }

  public void setPipelineFlexibility(
      PipelineFlexibility pipelineFlexibility) {
    this.pipelineFlexibility = pipelineFlexibility;
  }

  public PipelineMaterial getPipelineMaterial() {
    return pipelineMaterial;
  }

  public void setPipelineMaterial(PipelineMaterial pipelineMaterial) {
    this.pipelineMaterial = pipelineMaterial;
  }

  public String getOtherPipelineMaterialUsed() {
    return otherPipelineMaterialUsed;
  }

  public void setOtherPipelineMaterialUsed(String otherPipelineMaterialUsed) {
    this.otherPipelineMaterialUsed = otherPipelineMaterialUsed;
  }

  public Integer getPipelineDesignLife() {
    return pipelineDesignLife;
  }

  public void setPipelineDesignLife(Integer pipelineDesignLife) {
    this.pipelineDesignLife = pipelineDesignLife;
  }

  public Boolean getPipelineInBundle() {
    return pipelineInBundle;
  }

  public void setPipelineInBundle(Boolean pipelineInBundle) {
    this.pipelineInBundle = pipelineInBundle;
  }

  public String getBundleName() {
    return bundleName;
  }

  public void setBundleName(String bundleName) {
    this.bundleName = bundleName;
  }

  public String getWhyNotReturnedToShore() {
    return whyNotReturnedToShore;
  }

  public void setWhyNotReturnedToShore(String whyNotReturnedToShore) {
    this.whyNotReturnedToShore = whyNotReturnedToShore;
  }

  public Boolean getAlreadyExistsOnSeabed() {
    return alreadyExistsOnSeabed;
  }

  public void setAlreadyExistsOnSeabed(Boolean alreadyExistsOnSeabed) {
    this.alreadyExistsOnSeabed = alreadyExistsOnSeabed;
  }

  public Boolean getPipelineInUse() {
    return pipelineInUse;
  }

  public void setPipelineInUse(Boolean pipelineInUse) {
    this.pipelineInUse = pipelineInUse;
  }

  public String getFootnote() {
    return footnote;
  }

  public void setFootnote(String footnote) {
    this.footnote = footnote;
  }

}
