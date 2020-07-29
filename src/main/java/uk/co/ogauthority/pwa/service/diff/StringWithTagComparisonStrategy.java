package uk.co.ogauthority.pwa.service.diff;


import uk.co.ogauthority.pwa.model.diff.DiffType;
import uk.co.ogauthority.pwa.model.diff.DiffedField;
import uk.co.ogauthority.pwa.model.view.StringWithTag;

public class StringWithTagComparisonStrategy extends DiffComparisonStrategy<StringWithTag> {

  private final StringComparisonStrategy stringComparisonStrategy;

  StringWithTagComparisonStrategy() {
    super();
    this.stringComparisonStrategy = new StringComparisonStrategy();
  }

  @Override
  public StringWithTag objectAsType(Object value) {
    if (value instanceof StringWithTag) {
      return (StringWithTag) value;
    }

    throw new IllegalArgumentException(String.format("Cannot convert value of type '%s' as a StringWithTag", value.getClass()));
  }

  @Override
  public DiffedField compareType(StringWithTag currentValue, StringWithTag previousValue) {
    DiffedField diffedField = stringComparisonStrategy.compareType(currentValue.getValue(), previousValue.getValue());
    diffedField.setCurrentValueTag(currentValue.getTag());
    diffedField.setPreviousValueTag(previousValue.getTag());
    return diffedField;
  }

  @Override
  public DiffedField createTypeDeletedDiffedField(StringWithTag value) {
    return new DiffedField(DiffType.DELETED, null, value.getValue(), null, value.getTag());
  }

  @Override
  public DiffedField createTypeAddedDiffedField(StringWithTag value) {
    return new DiffedField(DiffType.ADDED, value.getValue(), null, value.getTag(), null);
  }
}

