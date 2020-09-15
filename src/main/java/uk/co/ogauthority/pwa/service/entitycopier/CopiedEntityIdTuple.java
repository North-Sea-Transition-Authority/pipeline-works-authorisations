package uk.co.ogauthority.pwa.service.entitycopier;

import java.util.Objects;

/**
 * A tuple representing the original Id of a copied entity and the new Id of the duplicate.
 *
 * @param <T> the type of id
 * @param <U> the class of the entity copied
 */
public class CopiedEntityIdTuple<T, U> {
  private final T originalEntityId;
  private final T duplicateEntityId;

  private final Class<U> entityClass;

  CopiedEntityIdTuple(Class<U> entityClass, T originalEntityId, T duplicateEntityId) {
    this.entityClass = entityClass;
    this.originalEntityId = originalEntityId;
    this.duplicateEntityId = duplicateEntityId;
  }

  public T getOriginalEntityId() {
    return originalEntityId;
  }

  public Class<U> getEntityClass() {
    return entityClass;
  }

  public T getDuplicateEntityId() {
    return duplicateEntityId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CopiedEntityIdTuple<?, ?> that = (CopiedEntityIdTuple<?, ?>) o;
    return Objects.equals(entityClass, that.entityClass)
        && Objects.equals(originalEntityId, that.originalEntityId)
        && Objects.equals(duplicateEntityId, that.duplicateEntityId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entityClass, originalEntityId, duplicateEntityId);
  }
}
