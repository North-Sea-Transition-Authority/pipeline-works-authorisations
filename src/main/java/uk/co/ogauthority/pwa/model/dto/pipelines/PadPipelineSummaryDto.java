package uk.co.ogauthority.pwa.model.dto.pipelines;

import java.math.BigDecimal;
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
 * Class designed to be constructed from jpa query to summarise a single PadPipeline.
 **/
public class PadPipelineSummaryDto {

  private final Integer padPipelineId;
  private final Integer pipelineId;
  private final String fromLocation;
  private final CoordinatePair fromCoordinates;
  private final String toLocation;
  private final CoordinatePair toCoordinates;
  private final String pipelineNumber;
  private final PipelineType pipelineType;
  private final String componentParts;
  private final BigDecimal length;
  private final String productsToBeConveyed;
  private final Long numberOfIdents;
  private final BigDecimal maxExternalDiameter;
  private final Boolean pipelineInBundle;
  private final String bundleName;
  private final PipelineFlexibility pipelineFlexibility;
  private final PipelineMaterial pipelineMaterial;
  private final String otherPipelineMaterialUsed;
  private final Boolean trenchedBuriedBackfilled;
  private final String trenchingMethodsDescription;
  private final PipelineStatus pipelineStatus;

  public PadPipelineSummaryDto(Integer padPipelineId,
                               Integer pipelineId,
                               PipelineType pipelineType,
                               String pipelineNumber,
                               BigDecimal length,
                               String componentParts,
                               String productsToBeConveyed,
                               Long numberOfIdents,
                               // From info.
                               String fromLocation,
                               Integer fromLatitudeDegrees,
                               Integer fromLatitudeMinutes,
                               BigDecimal fromLatitudeSeconds,
                               LatitudeDirection fromLatitudeDirection,
                               Integer fromLongitudeDegrees,
                               Integer fromLongitudeMinutes,
                               BigDecimal fromLongitudeSeconds,
                               LongitudeDirection fromLongitudeDirection,
                               // To info.
                               String toLocation,
                               Integer toLatitudeDegrees,
                               Integer toLatitudeMinutes,
                               BigDecimal toLatitudeSeconds,
                               LatitudeDirection toLatitudeDirection,
                               Integer toLongitudeDegrees,
                               Integer toLongitudeMinutes,
                               BigDecimal toLongitudeSeconds,
                               LongitudeDirection toLongitudeDirection,
                               BigDecimal maxExternalDiameter,
                               Boolean pipelineInBundle,
                               String bundleName,
                               PipelineFlexibility pipelineFlexibility,
                               PipelineMaterial pipelineMaterial,
                               String otherPipelineMaterialUsed,
                               Boolean trenchedBuriedBackfilled,
                               String trenchingMethodsDescription,
                               PipelineStatus pipelineStatus) {
    this.padPipelineId = padPipelineId;
    this.pipelineId = pipelineId;
    this.fromLocation = fromLocation;
    this.toLocation = toLocation;
    this.pipelineFlexibility = pipelineFlexibility;
    this.pipelineMaterial = pipelineMaterial;
    this.pipelineStatus = pipelineStatus;

    var fromLat = new LatitudeCoordinate(
        fromLatitudeDegrees,
        fromLatitudeMinutes,
        fromLatitudeSeconds,
        fromLatitudeDirection);

    var fromLong = new LongitudeCoordinate(
        fromLongitudeDegrees,
        fromLongitudeMinutes,
        fromLongitudeSeconds,
        fromLongitudeDirection);

    var toLat = new LatitudeCoordinate(
        toLatitudeDegrees,
        toLatitudeMinutes,
        toLatitudeSeconds,
        toLatitudeDirection);

    var toLong = new LongitudeCoordinate(
        toLongitudeDegrees,
        toLongitudeMinutes,
        toLongitudeSeconds,
        toLongitudeDirection);

    this.fromCoordinates = new CoordinatePair(fromLat, fromLong);

    this.toCoordinates = new CoordinatePair(toLat, toLong);

    this.pipelineNumber = pipelineNumber;
    this.pipelineType = pipelineType;
    this.componentParts = componentParts;
    this.length = length;
    this.productsToBeConveyed = productsToBeConveyed;
    this.numberOfIdents = numberOfIdents;
    this.maxExternalDiameter = maxExternalDiameter;
    this.pipelineInBundle = pipelineInBundle;
    this.bundleName = bundleName;
    this.otherPipelineMaterialUsed = otherPipelineMaterialUsed;
    this.trenchedBuriedBackfilled = trenchedBuriedBackfilled;
    this.trenchingMethodsDescription = trenchingMethodsDescription;
  }

  public int getPadPipelineId() {
    return padPipelineId;
  }

  public String getFromLocation() {
    return fromLocation;
  }

  public CoordinatePair getFromCoordinates() {
    return fromCoordinates;
  }

  public String getToLocation() {
    return toLocation;
  }

  public CoordinatePair getToCoordinates() {
    return toCoordinates;
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public PipelineType getPipelineType() {
    return pipelineType;
  }

  public String getComponentParts() {
    return componentParts;
  }

  public BigDecimal getLength() {
    return length;
  }

  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public Long getNumberOfIdents() {
    return numberOfIdents;
  }

  public PipelineId getPipelineId() {
    return new PipelineId(pipelineId);
  }

  public BigDecimal getMaxExternalDiameter() {
    return maxExternalDiameter;
  }

  public Boolean getPipelineInBundle() {
    return pipelineInBundle;
  }

  public String getBundleName() {
    return bundleName;
  }

  public PipelineFlexibility getPipelineFlexibility() {
    return pipelineFlexibility;
  }

  public PipelineMaterial getPipelineMaterial() {
    return pipelineMaterial;
  }

  public String getOtherPipelineMaterialUsed() {
    return otherPipelineMaterialUsed;
  }

  public Boolean getTrenchedBuriedBackfilled() {
    return trenchedBuriedBackfilled;
  }

  public String getTrenchingMethodsDescription() {
    return trenchingMethodsDescription;
  }

  public PipelineStatus getPipelineStatus() {
    return pipelineStatus;
  }
}
