package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

import java.util.Objects;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.diff.DiffableAsString;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

/**
 *  Wraps the data level unique identifier for a pipeline to prevent mistakes where primitive data type ids are passed around.
 */
public final class PipelineId implements PipelineIdentifier, DiffableAsString {
  private final int id;

  public PipelineId(int id) {
    this.id = id;
  }

  public static PipelineId from(PipelineOverview pipeline) {
    return new PipelineId(pipeline.getPipelineId());
  }

  public static PipelineId from(Pipeline pipeline) {
    return new PipelineId(pipeline.getId());
  }

  public static PipelineId from(NamedPipeline pipeline) {
    return new PipelineId(pipeline.getPipelineId());
  }

  public static PipelineId from(PipelineDetail pipelineDetail) {
    return pipelineDetail.getPipelineId();
  }

  public static PipelineId from(PadPipeline padPipeline) {
    return from(padPipeline.getPipeline());
  }

  public int asInt() {
    return this.id;
  }

  @Override
  public String getDiffableString() {
    return String.valueOf(id);
  }

  @Override
  public void accept(PipelineIdentifierVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public PipelineId getPipelineId() {
    return this;
  }

  @Override
  public int getPipelineIdAsInt() {
    return this.asInt();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineId that = (PipelineId) o;
    return id == that.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "PipelineId{" +
        "id=" + id +
        '}';
  }
}
