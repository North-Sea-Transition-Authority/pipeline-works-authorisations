package uk.co.ogauthority.pwa.service.diff;


import java.util.Objects;
import uk.co.ogauthority.pwa.model.diff.DiffType;
import uk.co.ogauthority.pwa.model.diff.DiffableAsString;
import uk.co.ogauthority.pwa.model.diff.DiffedField;
import uk.co.ogauthority.pwa.model.view.Tag;

public class ObjectDiffableAsStringStrategy extends DiffComparisonStrategy<DiffableAsString> {

  private final StringComparisonStrategy stringComparisonStrategy;

  ObjectDiffableAsStringStrategy() {
    super();
    this.stringComparisonStrategy = new StringComparisonStrategy();
  }

  @Override
  public DiffableAsString objectAsType(Object value) {
    if (Objects.isNull(value)) {
      return () -> "";
    } else if (value instanceof DiffableAsString) {
      return (DiffableAsString) value;
    }

    throw new IllegalArgumentException(
        String.format("Cannot convert value of type '%s' as a DiffableAsString", value.getClass()));
  }

  @Override
  public DiffedField compareType(DiffableAsString currentValue, DiffableAsString previousValue) {
    return stringComparisonStrategy.compareType(currentValue.getDiffableString(),
        previousValue.getDiffableString());
  }

  @Override
  public DiffedField createTypeDeletedDiffedField(DiffableAsString value) {
    return new DiffedField(DiffType.DELETED, null, value.getDiffableString(), null, Tag.NONE);
  }

  @Override
  public DiffedField createTypeAddedDiffedField(DiffableAsString value) {
    return new DiffedField(DiffType.ADDED, value.getDiffableString(), null, Tag.NONE, null);
  }
}

