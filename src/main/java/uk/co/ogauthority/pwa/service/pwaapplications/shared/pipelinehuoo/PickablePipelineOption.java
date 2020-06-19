package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/* Captures all the info required by the pipeline picker list view. Used by templates to render pipeline info */
public class PickablePipelineOption {

  private final int rawId;

  private final PickablePipelineType pickablePipelineType;
  private final String pipelineNumber;
  private final String pipelineTypeDisplay;
  private final String fromLocation;
  private final CoordinatePair fromCoordinates;
  private final String toLocation;
  private final CoordinatePair toCoordinates;
  private final String length;

  private final String pickableString;

  private PickablePipelineOption(int rawId,
                                 PickablePipelineType pickablePipelineType,
                                 String pipelineNumber,
                                 PipelineType pipelineType,
                                 String fromLocation,
                                 CoordinatePair fromCoordinates,
                                 String toLocation,
                                 CoordinatePair toCoordinates,
                                 BigDecimal length) {
    this.rawId = rawId;
    this.pickablePipelineType = pickablePipelineType;
    this.pipelineNumber = pipelineNumber;
    this.pipelineTypeDisplay = pipelineType != null ? pipelineType.getDisplayName() : "Pipeline type unavailable";
    this.fromLocation = fromLocation;
    this.fromCoordinates = fromCoordinates;
    this.toLocation = toLocation;
    this.toCoordinates = toCoordinates;
    this.length = length != null ? length.setScale(2, RoundingMode.HALF_UP)
        .toPlainString() + "m" : null;

    this.pickableString = this.pickablePipelineType.createIdString(rawId);

  }


  public static PickablePipelineOption from(PipelineDetail pipelineDetail) {
    var fromCoords = pipelineDetail.getFromLatitudeCoordinate().isPresent() && pipelineDetail.getFromLongitudeCoordinate().isPresent()
        ? new CoordinatePair(
        pipelineDetail.getFromLatitudeCoordinate().get(),
        pipelineDetail.getFromLongitudeCoordinate().get()
    ) : null;

    var toCoords = pipelineDetail.getToLatitudeCoordinate().isPresent() && pipelineDetail.getToLongitudeCoordinate().isPresent()
        ? new CoordinatePair(
        pipelineDetail.getToLatitudeCoordinate().get(),
        pipelineDetail.getToLongitudeCoordinate().get()
    ) : null;

    return new PickablePipelineOption(
        pipelineDetail.getPipelineId(),
        PickablePipelineType.CONSENTED,
        pipelineDetail.getPipelineNumber(),
        pipelineDetail.getPipelineType(),
        pipelineDetail.getFromLocation(),
        fromCoords,
        pipelineDetail.getToLocation(),
        toCoords,
        pipelineDetail.getLength()
    );
  }

  public static PickablePipelineOption from(PadPipelineSummaryDto padPipelineSummaryDto) {
    return new PickablePipelineOption(
        padPipelineSummaryDto.getPadPipelineId(),
        PickablePipelineType.APPLICATION,
        padPipelineSummaryDto.getPipelineNumber(),
        padPipelineSummaryDto.getPipelineType(),
        padPipelineSummaryDto.getFromLocation(),
        padPipelineSummaryDto.getFromCoordinates(),
        padPipelineSummaryDto.getToLocation(),
        padPipelineSummaryDto.getToCoordinates(),
        padPipelineSummaryDto.getLength()
    );
  }

  public int getRawId() {
    return rawId;
  }

  public String getPickableString() {
    return pickableString;
  }

  PickablePipelineType getPickablePipelineType() {
    return pickablePipelineType;
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public String getPipelineTypeDisplay() {
    return pipelineTypeDisplay;
  }

  public String getFromLocation() {
    return fromLocation;
  }

  public String getToLocation() {
    return toLocation;
  }

  public String getLength() {
    return length;
  }

  public Boolean hasFromCoordinates() {
    return this.fromCoordinates != null;
  }

  public Boolean hasToCoordinates() {
    return this.toCoordinates != null;
  }

  public CoordinatePair getFromCoordinates() {
    return fromCoordinates;
  }

  public CoordinatePair getToCoordinates() {
    return toCoordinates;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PickablePipelineOption that = (PickablePipelineOption) o;
    return rawId == that.rawId
        && pickablePipelineType == that.pickablePipelineType
        && Objects.equals(pipelineNumber, that.pipelineNumber)
        && Objects.equals(pipelineTypeDisplay, that.pipelineTypeDisplay)
        && Objects.equals(fromLocation, that.fromLocation)
        && Objects.equals(fromCoordinates, that.fromCoordinates)
        && Objects.equals(toLocation, that.toLocation)
        && Objects.equals(toCoordinates, that.toCoordinates)
        && Objects.equals(length, that.length)
        && Objects.equals(pickableString, that.pickableString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rawId, pickablePipelineType, pipelineNumber, pipelineTypeDisplay, fromLocation, fromCoordinates,
        toLocation, toCoordinates, length, pickableString);
  }
}
