package uk.co.ogauthority.pwa.model.dto.consultations;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;

/**
 * Simple dto class to transfer id of consultee groups.
 */
public class ConsulteeGroupId {
  private final int id;

  ConsulteeGroupId(int id) {
    this.id = id;
  }

  public static ConsulteeGroupId from(ConsulteeGroup consulteeGroup) {
    return new ConsulteeGroupId(consulteeGroup.getId());
  }

  public static ConsulteeGroupId from(Integer consulteeGroupId) {
    return new ConsulteeGroupId(consulteeGroupId);
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
    ConsulteeGroupId that = (ConsulteeGroupId) o;
    return id == that.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
