package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import java.util.Objects;

/**
 * Wraps the data level unique identifier for an application pipeline to prevent mistakes where primitive data type ids are passed around.
 */
public final class PadPipelineId {

  private final int id;

  public PadPipelineId(int id) {
    this.id = id;
  }

  public static PadPipelineId from(PadPipeline padPipeline) {
    return new PadPipelineId(padPipeline.getId());
  }

  public int asInt() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadPipelineId that = (PadPipelineId) o;
    return id == that.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "PadPipelineId{" +
        "id=" + id +
        '}';
  }
}
