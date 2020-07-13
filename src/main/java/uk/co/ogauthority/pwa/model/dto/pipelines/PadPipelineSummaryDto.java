package uk.co.ogauthority.pwa.model.dto.pipelines;

import java.math.BigDecimal;
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

  private final String pipelineName;

  private final PipelineType pipelineType;

  private final String componentParts;

  private final BigDecimal length;

  private final String productsToBeConveyed;

  private final Long numberOfIdents;

  public PadPipelineSummaryDto(Integer padPipelineId,
                               Integer pipelineId,
                               String pipelineName,
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
                               LongitudeDirection toLongitudeDirection


  ) {
    this.padPipelineId = padPipelineId;
    this.pipelineId = pipelineId;
    this.pipelineName = pipelineName;
    this.fromLocation = fromLocation;
    this.toLocation = toLocation;

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
  }

  public int getPadPipelineId() {
    return padPipelineId;
  }

  public String getPipelineName() {
    return pipelineName;
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

  public Integer getPipelineId() {
    return pipelineId;
  }


}
