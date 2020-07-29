package uk.co.ogauthority.pwa.service.diff;

import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.model.diff.DiffType;
import uk.co.ogauthority.pwa.model.diff.DiffedField;

public class StringComparisonStrategy extends DiffComparisonStrategy<String> {

  StringComparisonStrategy() {
    super();
  }

  @Override
  public String objectAsType(Object value) {

    if (value == null) {
      return "";
    }

    if (value instanceof String) {
      return (String) value;
    }

    if (value instanceof Integer) {
      Integer current = (Integer) value;
      return current.toString();
    }

    throw new IllegalArgumentException(String.format("Cannot represent value of type '%s' as String", value.getClass()));
  }

  @Override
  public DiffedField compareType(String currentValue, String previousValue) {
    if (currentValue.equals(previousValue)) {
      return new DiffedField(DiffType.UNCHANGED, currentValue, previousValue);
    }

    if (StringUtils.isBlank(currentValue) && StringUtils.isNotBlank(previousValue)) {
      return new DiffedField(DiffType.DELETED, currentValue, previousValue);
    }

    if (StringUtils.isNotBlank(currentValue) && StringUtils.isBlank(previousValue)) {
      return new DiffedField(DiffType.ADDED, currentValue, previousValue);
    }

    return new DiffedField(DiffType.UPDATED, currentValue, previousValue);
  }

  @Override
  public DiffedField createTypeDeletedDiffedField(String value) {
    return new DiffedField(DiffType.DELETED, "", value);
  }

  @Override
  public DiffedField createTypeAddedDiffedField(String value) {
    return new DiffedField(DiffType.ADDED, value, "");
  }
}

