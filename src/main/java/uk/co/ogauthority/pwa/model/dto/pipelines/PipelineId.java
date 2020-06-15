package uk.co.ogauthority.pwa.model.dto.pipelines;

import java.util.Objects;

/* Wraps the data level unique identifier for a pipeline to prevent mistakes where primitive data type ids are passed around.*/
public final class PipelineId {
  private final int id;

  public PipelineId(int id) {
    this.id = id;
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
    PipelineId that = (PipelineId) o;
    return id == that.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
