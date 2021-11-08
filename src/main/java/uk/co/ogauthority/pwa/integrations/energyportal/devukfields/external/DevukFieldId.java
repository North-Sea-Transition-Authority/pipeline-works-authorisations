package uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external;

import java.util.Objects;

/**
 * Represents the consistent unique ID of a DEVUK Field.
 */
public class DevukFieldId {

  private final int fieldId;

  public DevukFieldId(int fieldId) {
    this.fieldId = fieldId;
  }

  public int asInt() {
    return fieldId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DevukFieldId that = (DevukFieldId) o;
    return fieldId == that.fieldId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(fieldId);
  }
}
