package uk.co.ogauthority.pwa.model.dto.pipelines;

import java.util.Objects;

/**
 * Represents a portion of a pipeline between two points on the pipelines path.
 */
public class PipelineSection implements PipelineIdentifier {

  private final PipelineId pipelineId;
  private final int sectionNumber;
  private final PipelineIdentPoint fromPoint;
  private final PipelineIdentPoint toPoint;

  // private constructor to force static method use on object creation
  private PipelineSection(PipelineId pipelineId,
                          int sectionNumber,
                          PipelineIdentPoint fromPoint,
                          PipelineIdentPoint toPoint) {
    this.pipelineId = pipelineId;
    this.fromPoint = fromPoint;
    this.toPoint = toPoint;
    this.sectionNumber = sectionNumber;
  }

  public static PipelineSection from(int pipelineId,
                                     String fromPoint,
                                     IdentLocationInclusionMode fromPointInclusionMode,
                                     String toPoint,
                                     IdentLocationInclusionMode toPointInclusionMode,
                                     int sectionNumber) {
    return new PipelineSection(
        new PipelineId(pipelineId),
        sectionNumber,
        PipelineIdentPoint.from(fromPoint, fromPointInclusionMode),
        PipelineIdentPoint.from(toPoint, toPointInclusionMode)
    );
  }

  public static PipelineSection from(PipelineId pipelineId,
                                     int segmentNumber,
                                     PipelineIdentPoint fromPoint,
                                     PipelineIdentPoint toPoint) {
    return new PipelineSection(
        pipelineId,
        segmentNumber,
        fromPoint,
        toPoint
    );
  }


  public String getDisplayString() {
    return String.format("%s %s", fromPoint.getFromPointDisplayString(), toPoint.getToPointDisplayString());
  }

  @Override
  public void accept(PipelineIdentifierVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public PipelineId getPipelineId() {
    return pipelineId;
  }

  @Override
  public int getPipelineIdAsInt() {
    return pipelineId.asInt();
  }

  public IdentLocationInclusionMode getFromPointMode() {
    return fromPoint.getIdentLocationInclusionMode();
  }

  public IdentLocationInclusionMode getToPointMode() {
    return toPoint.getIdentLocationInclusionMode();
  }

  public PipelineIdentPoint getFromPoint() {
    return fromPoint;
  }

  public PipelineIdentPoint getToPoint() {
    return toPoint;
  }

  public int getSectionNumber() {
    return sectionNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineSection that = (PipelineSection) o;
    return sectionNumber == that.sectionNumber
        && Objects.equals(pipelineId, that.pipelineId)
        && Objects.equals(fromPoint, that.fromPoint)
        && Objects.equals(toPoint, that.toPoint);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineId, sectionNumber, fromPoint, toPoint);
  }
}
