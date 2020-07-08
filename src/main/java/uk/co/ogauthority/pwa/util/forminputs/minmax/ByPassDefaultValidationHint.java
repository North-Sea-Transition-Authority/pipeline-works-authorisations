package uk.co.ogauthority.pwa.util.forminputs.minmax;

/*
  This class should be used as a validation hint passed to the MinMaxInputValidator within a List to
   indicate what default rules of the min max validation should be by passed.
 */

public final class ByPassDefaultValidationHint {

  DefaultValidationRule defaultValidationRule;

  public ByPassDefaultValidationHint(
      DefaultValidationRule defaultValidationRule) {
    this.defaultValidationRule = defaultValidationRule;
  }
}
