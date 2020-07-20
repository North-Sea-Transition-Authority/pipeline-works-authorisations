package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

/**
 * Represents a pickable pipeline from either application or consented model in a stringifiable form.
 */
public class PickablePipelineId {

  private final String id;

  private PickablePipelineId(String id) {
    this.id = id;
  }

  public static PickablePipelineId from(String stringId) {
    return new PickablePipelineId(stringId);
  }


  public static PickablePipelineId from(PadPipeline padPipeline) {
    return new PickablePipelineId(PickablePipelineType.APPLICATION.createIdString(padPipeline.getId()));
  }

  public static PickablePipelineId from(PickablePipelineOption pickablePipelineOption) {
    return new PickablePipelineId(pickablePipelineOption.getPickableString());
  }

  boolean isApplicationPipelineId() {
    return PickablePipelineType.getTypeIdFromString(this.id).equals(PickablePipelineType.APPLICATION);
  }

  boolean isConsentedPipelineId() {
    return PickablePipelineType.getTypeIdFromString(this.id).equals(PickablePipelineType.CONSENTED);
  }

  Integer getIdAsIntOrNull() {
    return PickablePipelineType.getIntegerIdFromString(this.id);
  }

  public String getId() {
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
    PickablePipelineId that = (PickablePipelineId) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
