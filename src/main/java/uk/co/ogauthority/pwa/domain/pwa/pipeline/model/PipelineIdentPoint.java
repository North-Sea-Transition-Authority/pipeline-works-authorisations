package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

import java.util.Objects;

/**
 * Represents a single point along the path of a pipeline.
 */
public class PipelineIdentPoint {

  private String locationName;
  private IdentLocationInclusionMode identLocationInclusionMode;

  private PipelineIdentPoint(String locationName,
                             IdentLocationInclusionMode identLocationInclusionMode) {
    this.locationName = locationName;
    this.identLocationInclusionMode = identLocationInclusionMode;
  }

  public static PipelineIdentPoint from(String locationName,
                                        IdentLocationInclusionMode identLocationInclusionMode) {
    return new PipelineIdentPoint(locationName, identLocationInclusionMode);
  }

  public static PipelineIdentPoint inclusivePoint(String location) {
    return new PipelineIdentPoint(location, IdentLocationInclusionMode.INCLUSIVE);
  }

  public static PipelineIdentPoint exclusivePoint(String location) {
    return new PipelineIdentPoint(location, IdentLocationInclusionMode.EXCLUSIVE);
  }

  public String getLocationName() {
    return locationName;
  }

  public IdentLocationInclusionMode getIdentLocationInclusionMode() {
    return identLocationInclusionMode;
  }

  public String getFromPointDisplayString() {
    return this.identLocationInclusionMode.getFromLocationDisplayString(this.locationName);
  }

  public String getToPointDisplayString() {
    return this.identLocationInclusionMode.getToLocationDisplayString(this.locationName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineIdentPoint that = (PipelineIdentPoint) o;
    return Objects.equals(locationName, that.locationName)
        && identLocationInclusionMode == that.identLocationInclusionMode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(locationName, identLocationInclusionMode);
  }

  @Override
  public String toString() {
    return "PipelineIdentPoint{" +
        "identPointName='" + locationName + '\'' +
        ", identPointInclusionMode=" + identLocationInclusionMode +
        '}';
  }
}
