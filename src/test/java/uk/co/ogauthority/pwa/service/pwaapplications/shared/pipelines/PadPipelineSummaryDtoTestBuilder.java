package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineFlexibility;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

/**
 * Provides a way to create a PadPipelineSummaryDto without having to provide all parameters.
 */
public class PadPipelineSummaryDtoTestBuilder {

  private Integer padPipelineId = 0;
  private Integer pipelineId = 0;
  private String fromLocation = null;
  private CoordinatePair fromCoordinates = new CoordinatePair(
      new LatitudeCoordinate(0, 0, BigDecimal.ZERO, LatitudeDirection.NORTH),
      new LongitudeCoordinate(0, 0, BigDecimal.ZERO, LongitudeDirection.EAST)
  );
  private String toLocation = null;
  private CoordinatePair toCoordinates = new CoordinatePair(
      new LatitudeCoordinate(0, 0, BigDecimal.ZERO, LatitudeDirection.NORTH),
      new LongitudeCoordinate(0, 0, BigDecimal.ZERO, LongitudeDirection.EAST)
  );
  private String pipelineNumber = null;
  private PipelineType pipelineType = null;
  private String componentParts = null;
  private BigDecimal length = null;
  private String productsToBeConveyed = null;
  private Long numberOfIdents = null;
  private BigDecimal maxExternalDiameter = null;
  private Boolean pipelineInBundle = null;
  private String bundleName = null;
  private PipelineFlexibility pipelineFlexibility = null;
  private PipelineMaterial pipelineMaterial = null;
  private String otherPipelineMaterialUsed = null;
  private Boolean trenchedBuriedBackfilled = null;
  private String trenchingMethodsDescription = null;
  private PipelineStatus pipelineStatus = null;
  private String pipelineStatusReason = null;

  public PadPipelineSummaryDtoTestBuilder setPadPipelineId(Integer padPipelineId) {
    this.padPipelineId = padPipelineId;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setPipelineId(Integer pipelineId) {
    this.pipelineId = pipelineId;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setFromLocation(String fromLocation) {
    this.fromLocation = fromLocation;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setFromCoordinates(
      CoordinatePair fromCoordinates) {
    this.fromCoordinates = fromCoordinates;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setToLocation(String toLocation) {
    this.toLocation = toLocation;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setToCoordinates(
      CoordinatePair toCoordinates) {
    this.toCoordinates = toCoordinates;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setPipelineNumber(String pipelineNumber) {
    this.pipelineNumber = pipelineNumber;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setPipelineType(
      PipelineType pipelineType) {
    this.pipelineType = pipelineType;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setComponentParts(String componentParts) {
    this.componentParts = componentParts;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setLength(BigDecimal length) {
    this.length = length;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setNumberOfIdents(Long numberOfIdents) {
    this.numberOfIdents = numberOfIdents;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setMaxExternalDiameter(BigDecimal maxExternalDiameter) {
    this.maxExternalDiameter = maxExternalDiameter;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setPipelineInBundle(Boolean pipelineInBundle) {
    this.pipelineInBundle = pipelineInBundle;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setBundleName(String bundleName) {
    this.bundleName = bundleName;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setPipelineFlexibility(
      PipelineFlexibility pipelineFlexibility) {
    this.pipelineFlexibility = pipelineFlexibility;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setPipelineMaterial(
      PipelineMaterial pipelineMaterial) {
    this.pipelineMaterial = pipelineMaterial;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setOtherPipelineMaterialUsed(String otherPipelineMaterialUsed) {
    this.otherPipelineMaterialUsed = otherPipelineMaterialUsed;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setTrenchedBuriedBackfilled(Boolean trenchedBuriedBackfilled) {
    this.trenchedBuriedBackfilled = trenchedBuriedBackfilled;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setTrenchingMethodsDescription(String trenchingMethodsDescription) {
    this.trenchingMethodsDescription = trenchingMethodsDescription;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setPipelineStatus(
      PipelineStatus pipelineStatus) {
    this.pipelineStatus = pipelineStatus;
    return this;
  }

  public PadPipelineSummaryDtoTestBuilder setPipelineStatusReason(String pipelineStatusReason) {
    this.pipelineStatusReason = pipelineStatusReason;
    return this;
  }

  public PadPipelineSummaryDto build() {
    return new PadPipelineSummaryDto(padPipelineId, pipelineId, pipelineType, pipelineNumber, length, componentParts,
        productsToBeConveyed, numberOfIdents, fromLocation, fromCoordinates.getLatitude().getDegrees(),
        fromCoordinates.getLatitude().getMinutes(), fromCoordinates.getLatitude().getSeconds(),
        fromCoordinates.getLatitude().getDirection(),
        fromCoordinates.getLongitude().getDegrees(), fromCoordinates.getLongitude().getMinutes(),
        fromCoordinates.getLongitude().getSeconds(),
        fromCoordinates.getLongitude().getDirection(), toLocation, toCoordinates.getLatitude().getDegrees(),
        toCoordinates.getLatitude().getMinutes(), toCoordinates.getLatitude().getSeconds(),
        toCoordinates.getLatitude().getDirection(), toCoordinates.getLongitude().getDegrees(),
        toCoordinates.getLongitude().getMinutes(),
        toCoordinates.getLongitude().getSeconds(), toCoordinates.getLongitude().getDirection(), maxExternalDiameter,
        pipelineInBundle, bundleName, pipelineFlexibility, pipelineMaterial, otherPipelineMaterialUsed,
        trenchedBuriedBackfilled, trenchingMethodsDescription, pipelineStatus, pipelineStatusReason);
  }
}
