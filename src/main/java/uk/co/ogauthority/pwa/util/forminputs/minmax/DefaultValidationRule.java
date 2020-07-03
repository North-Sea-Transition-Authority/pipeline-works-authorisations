package uk.co.ogauthority.pwa.util.forminputs.minmax;


public enum DefaultValidationRule {

  MIN_SMALLER_THAN_MAX("Min smaller or equal to max");

  private final String displayText;

  DefaultValidationRule(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }


}
