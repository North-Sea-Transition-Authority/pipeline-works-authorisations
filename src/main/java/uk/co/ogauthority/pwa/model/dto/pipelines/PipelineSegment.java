package uk.co.ogauthority.pwa.model.dto.pipelines;

import java.util.Objects;

/**
 * Represents a portion of a pipeline between two points on the pipelines path.
 */
public class PipelineSegment implements PipelineIdentifier {

  private final PipelineId pipelineId;
  private final PipelineIdentPoint fromPoint;
  private final PipelineIdentPoint toPoint;

  // private constructor to force static method use on object creation
  private PipelineSegment(PipelineId pipelineId,
                          PipelineIdentPoint fromPoint,
                          PipelineIdentPoint toPoint) {
    this.pipelineId = pipelineId;
    this.fromPoint = fromPoint;
    this.toPoint = toPoint;
  }

  public static PipelineSegment from(int pipelineId,
                                     String fromPoint,
                                     IdentLocationInclusionMode fromPointInclusionMode,
                                     String toPoint,
                                     IdentLocationInclusionMode toPointInclusionMode) {
    return new PipelineSegment(
        new PipelineId(pipelineId),
        PipelineIdentPoint.from(fromPoint, fromPointInclusionMode),
        PipelineIdentPoint.from(toPoint, toPointInclusionMode)
    );
  }

  public static PipelineSegment from(PipelineId pipelineId,
                                     PipelineIdentPoint fromPoint,
                                     PipelineIdentPoint toPoint) {
    return new PipelineSegment(
        pipelineId,
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineSegment that = (PipelineSegment) o;
    return Objects.equals(pipelineId, that.pipelineId)
        && Objects.equals(fromPoint, that.fromPoint)
        && Objects.equals(toPoint, that.toPoint);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineId, fromPoint, toPoint);
  }
}
