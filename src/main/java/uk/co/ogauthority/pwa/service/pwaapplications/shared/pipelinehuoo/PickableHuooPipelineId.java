package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import java.util.Objects;
import java.util.Optional;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;

/**
 * Represents a pickable pipeline for the pipeline huoo screen in a stringified form.
 * To be used in conjunction with {@link PickableHuooPipelineType}
 */
public class PickableHuooPipelineId {

  private final String id;

  private PickableHuooPipelineId(String id) {
    this.id = id;
  }

  public static PickableHuooPipelineId from(String stringId) {
    return new PickableHuooPipelineId(stringId);
  }


  public Optional<PipelineIdentifier> decodePickableStringId() {
    return PickableHuooPipelineType.decodeString(this.id);
  }

  public String asString() {
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
    PickableHuooPipelineId that = (PickableHuooPipelineId) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
