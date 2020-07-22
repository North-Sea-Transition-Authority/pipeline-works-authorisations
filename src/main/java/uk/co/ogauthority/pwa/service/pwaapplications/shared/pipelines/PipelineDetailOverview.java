package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

public class PipelineDetailOverview implements PipelineOverview {

  private Integer pipelineId;
  String fromLocation;
  CoordinatePair fromCoordinates;
  String toLocation;
  CoordinatePair toCoordinates;
  String pipelineNumber;
  PipelineType pipelineType;
  String componentPartsDesc;
  BigDecimal length;
  String productsToBeConveyed;
  Long numberOfIdents;
  BigDecimal maxExternalDiameter;
  Boolean pipelineInBundle;
  String bundleName;

  public PipelineDetailOverview(PipelineDetail detail, PipelineDetailIdentCountDto countDto) {
    this.pipelineId = detail.getPipelineId();
    this.fromLocation = detail.getFromLocation();
    this.fromCoordinates = detail.getFromCoordinates();
    this.toLocation = detail.getToLocation();
    this.toCoordinates = detail.getToCoordinates();
    this.pipelineNumber = detail.getPipelineNumber();
    this.pipelineType = detail.getPipelineType();
    this.componentPartsDesc = detail.getComponentPartsDesc();
    this.length = detail.getLength();
    this.productsToBeConveyed = detail.getProductsToBeConveyed();
    this.maxExternalDiameter = detail.getMaxExternalDiameter();
    this.pipelineInBundle = detail.getPipelineInBundle();
    this.bundleName = detail.getBundleName();
    if (countDto == null) {
      this.numberOfIdents = 0L;
    } else {
      this.numberOfIdents = countDto.getIdentCount();
    }
  }

  @Override
  public Integer getPadPipelineId() {
    return null;
  }

  @Override
  public Integer getPipelineId() {
    return null;
  }

  @Override
  public String getFromLocation() {
    return null;
  }

  @Override
  public CoordinatePair getFromCoordinates() {
    return null;
  }

  @Override
  public String getToLocation() {
    return null;
  }

  @Override
  public CoordinatePair getToCoordinates() {
    return null;
  }

  @Override
  public String getPipelineNumber() {
    return null;
  }

  @Override
  public PipelineType getPipelineType() {
    return null;
  }

  @Override
  public String getComponentParts() {
    return null;
  }

  @Override
  public BigDecimal getLength() {
    return null;
  }

  @Override
  public String getProductsToBeConveyed() {
    return null;
  }

  @Override
  public Long getNumberOfIdents() {
    return null;
  }

  @Override
  public BigDecimal getMaxExternalDiameter() {
    return null;
  }

  @Override
  public Boolean getPipelineInBundle() {
    return null;
  }

  @Override
  public String getBundleName() {
    return null;
  }
}
