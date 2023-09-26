package uk.co.ogauthority.pwa.service.search.consents.tabcontentviews;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;

public class PwaPipelineView {

  private final Integer pipelineId;
  private final String pipelineNumber;
  private final PipelineStatus status;
  private final AsBuiltNotificationStatus asBuiltNotificationStatus;
  private final String fromLocation;
  private final CoordinatePair fromCoordinates;
  private final String toLocation;
  private final CoordinatePair toCoordinates;
  private final String length;
  private final String transferredFromPwaRef;
  private final String transferredFromPwaUrl;
  private final String transferredToPwaRef;
  private final String transferredToPwaUrl;

  public PwaPipelineView(PipelineOverview pipelineOverview,
                         String transferredFromPwaRef,
                         String transferredFromPwaUrl,
                         String transferredToPwaRef,
                         String transferredToPwaUrl) {
    this.pipelineId = pipelineOverview.getPipelineId();
    this.pipelineNumber = pipelineOverview.getPipelineNumber();
    this.status = pipelineOverview.getPipelineStatus();
    this.fromLocation = pipelineOverview.getFromLocation();
    this.fromCoordinates = pipelineOverview.getFromCoordinates();
    this.toLocation = pipelineOverview.getToLocation();
    this.toCoordinates = pipelineOverview.getToCoordinates();
    this.length = createLengthDisplayString(pipelineOverview.getLength());
    this.asBuiltNotificationStatus = pipelineOverview.getAsBuiltNotificationStatus();
    this.transferredFromPwaRef = transferredFromPwaRef;
    this.transferredFromPwaUrl = transferredFromPwaUrl;
    this.transferredToPwaRef = transferredToPwaRef;
    this.transferredToPwaUrl = transferredToPwaUrl;
  }

  public Integer getPipelineId() {
    return pipelineId;
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public PipelineStatus getStatus() {
    return status;
  }

  public AsBuiltNotificationStatus getAsBuiltNotificationStatus() {
    return asBuiltNotificationStatus;
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

  public String getLength() {
    return length;
  }

  public String getTransferredFromPwaRef() {
    return transferredFromPwaRef;
  }

  public String getTransferredToPwaRef() {
    return transferredToPwaRef;
  }

  public String getTransferredFromPwaUrl() {
    return transferredFromPwaUrl;
  }

  public String getTransferredToPwaUrl() {
    return transferredToPwaUrl;
  }

  private static String createLengthDisplayString(BigDecimal length) {
    return length != null ? length.setScale(2, RoundingMode.HALF_UP).toPlainString() : null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PwaPipelineView that = (PwaPipelineView) o;
    return Objects.equals(pipelineNumber, that.pipelineNumber)
        && status == that.status
        && Objects.equals(fromLocation, that.fromLocation)
        && Objects.equals(fromCoordinates, that.fromCoordinates)
        && Objects.equals(toLocation, that.toLocation)
        && Objects.equals(toCoordinates, that.toCoordinates)
        && Objects.equals(length, that.length)
        && Objects.equals(transferredFromPwaRef, that.transferredFromPwaRef)
        && Objects.equals(transferredFromPwaUrl, that.transferredFromPwaUrl)
        && Objects.equals(transferredToPwaRef, that.transferredToPwaRef)
        && Objects.equals(transferredToPwaUrl, that.transferredToPwaUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineNumber, status, fromLocation, fromCoordinates, toLocation, toCoordinates, length,
        transferredFromPwaRef, transferredFromPwaUrl, transferredToPwaRef, transferredToPwaUrl);
  }

}
