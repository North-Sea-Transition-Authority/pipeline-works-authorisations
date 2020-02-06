package uk.co.ogauthority.pwa.energyportal.model.entity;

import java.util.Objects;

/**
 * Wrapper for an numeric Person ID.
 * This allows typesafe access/differentiation between the various numeric resource IDs (wua_id, rp_id etc)
 */
public class PersonId {

  private final int id;

  public PersonId(int id) {
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
    PersonId personId = (PersonId) o;
    return id == personId.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
