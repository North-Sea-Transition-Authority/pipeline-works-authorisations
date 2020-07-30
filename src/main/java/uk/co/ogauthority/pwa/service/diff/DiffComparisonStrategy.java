package uk.co.ogauthority.pwa.service.diff;

import uk.co.ogauthority.pwa.model.diff.DiffedField;

/**
 * How to diff objects of type T. For use by {@link DiffComparisonTypes} and {@link DiffService}
 */
public abstract class DiffComparisonStrategy<T> {

  /**
   * create a diffedField object where the diff type is DELETED.
   */
  DiffedField createDeletedDiffedField(Object value) {
    return createTypeDeletedDiffedField(objectAsType(value));
  }

  /**
   * create a diffedField object where the diff type is ADDED.
   */
  DiffedField createAddedDiffedField(Object value) {
    return createTypeAddedDiffedField(objectAsType(value));
  }

  /**
   * This is the attach point the diff service code uses. The Implementation detail is left to each strategy.
   */
  public DiffedField compare(Object currentValue, Object previousValue) {
    return compareType(objectAsType(currentValue), objectAsType(previousValue));
  }

  /**
   * Convert an object into type T. Throws IllegalArgumentException when object class not supported.
   */
  abstract T objectAsType(Object value);

  /**
   * Do type specific comparison steps to construct a DiffedField.
   *
   * @return diffed field representation of difference.
   */
  abstract DiffedField compareType(T currentValue, T previousValue);

  /**
   * Type specific representation of deleted value.
   */
  abstract DiffedField createTypeDeletedDiffedField(T value);

  /**
   * Type specific representation of added value.
   */
  abstract DiffedField createTypeAddedDiffedField(T value);
}
