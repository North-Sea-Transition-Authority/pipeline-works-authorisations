package uk.co.ogauthority.pwa.util.forminputs.minmax;

/*
  This class should be used as a validation hint passed to the MinMaxInputValidator within a List to
   indicate what default rules of the min max validation should be by passed.
 */

import java.util.Objects;

public final class ByPassDefaultValidationHint {

  private DefaultValidationRule defaultValidationRule;

  public ByPassDefaultValidationHint(
      DefaultValidationRule defaultValidationRule) {
    this.defaultValidationRule = defaultValidationRule;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ByPassDefaultValidationHint that = (ByPassDefaultValidationHint) o;
    return defaultValidationRule == that.defaultValidationRule;
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultValidationRule);
  }
}
