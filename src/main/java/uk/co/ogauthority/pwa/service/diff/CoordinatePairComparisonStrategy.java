package uk.co.ogauthority.pwa.service.diff;


import uk.co.ogauthority.pwa.model.diff.DiffType;
import uk.co.ogauthority.pwa.model.diff.DiffedField;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.view.Tag;

public class CoordinatePairComparisonStrategy extends DiffComparisonStrategy<CoordinatePair> {

  private final StringComparisonStrategy stringComparisonStrategy;

  CoordinatePairComparisonStrategy() {
    super();
    this.stringComparisonStrategy = new StringComparisonStrategy();
  }

  @Override
  public CoordinatePair objectAsType(Object value) {
    if (value instanceof CoordinatePair) {
      return (CoordinatePair) value;
    }

    throw new IllegalArgumentException(String.format("Cannot convert value of type '%s' as a CoordinatePair", value.getClass()));
  }

  @Override
  public DiffedField compareType(CoordinatePair currentValue, CoordinatePair previousValue) {
    DiffedField diffedField = stringComparisonStrategy.compareType(currentValue.getDisplayString(), previousValue.getDisplayString());
    return diffedField;
  }

  @Override
  public DiffedField createTypeDeletedDiffedField(CoordinatePair value) {
    return new DiffedField(DiffType.DELETED, null, value.getDisplayString(), null, Tag.NONE);
  }

  @Override
  public DiffedField createTypeAddedDiffedField(CoordinatePair value) {
    return new DiffedField(DiffType.ADDED, value.getDisplayString(), null, Tag.NONE, null);
  }
}

