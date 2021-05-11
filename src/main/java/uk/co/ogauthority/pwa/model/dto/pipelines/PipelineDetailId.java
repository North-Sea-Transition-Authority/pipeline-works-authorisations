package uk.co.ogauthority.pwa.model.dto.pipelines;


import java.util.Objects;

/**
 * Wraps the id of a pipeline detail to allow type safe passing around of ids with and help to avoids
 * loading in entity graphs when not needed.
 */
public final class PipelineDetailId {

  private final int id;

  public PipelineDetailId(int id) {
    this.id = id;
  }

  public int asInt() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineDetailId that = (PipelineDetailId) o;
    return id == that.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
