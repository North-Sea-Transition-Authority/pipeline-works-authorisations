package uk.co.ogauthority.pwa.service.search.consents.tabcontentviews;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

public class PwaPipelineView implements Comparable<PwaPipelineView> {

  private final Integer pipelineId;
  private final String pipelineNumber;
  private final PipelineStatus status;
  private final AsBuiltNotificationStatus asBuiltNotificationStatus;
  private final String fromLocation;
  private final CoordinatePair fromCoordinates;
  private final String toLocation;
  private final CoordinatePair toCoordinates;
  private final String length;

  private static final Logger LOGGER = LoggerFactory.getLogger(PwaPipelineView.class);


  public PwaPipelineView(PipelineOverview pipelineOverview) {
    this.pipelineId = pipelineOverview.getPipelineId();
    this.pipelineNumber = pipelineOverview.getPipelineNumber();
    this.status = pipelineOverview.getPipelineStatus();
    this.fromLocation = pipelineOverview.getFromLocation();
    this.fromCoordinates = pipelineOverview.getFromCoordinates();
    this.toLocation = pipelineOverview.getToLocation();
    this.toCoordinates = pipelineOverview.getToCoordinates();
    this.length = createLengthDisplayString(pipelineOverview.getLength());
    this.asBuiltNotificationStatus = pipelineOverview.getAsBuiltNotificationStatus();
  }

  public Integer getPipelineId() {
    return pipelineId;
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  private String getPipelineNumberOnlyFromReference() {

    StringBuilder pipelineNumberValue = new StringBuilder();
    for (Character character: pipelineNumber.toCharArray()) {

      if (Character.isDigit(character) || character.equals('.')) {
        pipelineNumberValue.append(character);

      } else if (pipelineNumberValue.length() > 0) {
        break;
      }
    }

    return pipelineNumberValue.toString();
  }

  private String getPipelineNumberWithoutPrefix() {

    return pipelineNumber.replace("PLU", "")
        .replace("PL", "")
        .trim();
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
        && Objects.equals(length, that.length);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineNumber, status, fromLocation, fromCoordinates, toLocation, toCoordinates, length);
  }

  @Override
  public int compareTo(PwaPipelineView comparingPwaPipelineView) {


    try {
      var pipelineNumberValue = new BigDecimal(this.getPipelineNumberOnlyFromReference());
      var comparingPipelineNumberValue = new BigDecimal(comparingPwaPipelineView.getPipelineNumberOnlyFromReference());
      var comparisonResult = pipelineNumberValue.compareTo(comparingPipelineNumberValue);

      if (comparisonResult == 0) {
        //the pipeline numeric values are equal, need to compare by their suffix
        return this.getPipelineNumberWithoutPrefix().compareTo(comparingPwaPipelineView.getPipelineNumberWithoutPrefix());
      }

      return comparisonResult;

    } catch (NumberFormatException e) {

      LOGGER.error("Could not extract a numeric value from the pipeline numbers: {} and {}. " +
              "Falling back to default String comparison of the pipeline number",
          this.getPipelineNumber(), comparingPwaPipelineView.getPipelineNumber());

      return this.getPipelineNumber().compareTo(comparingPwaPipelineView.getPipelineNumber());
    }

  }


}
