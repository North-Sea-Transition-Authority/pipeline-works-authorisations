package uk.co.ogauthority.pwa.util.forminputs.minmax;

/*
  This class should be used as a validation hint passed to the MinMaxInputValidator within a List to
   indicate that the minMax values should be validated to be at a maximum decimal place
   which is specified as the number passed to this class' constructor.
 */
public final class DecimalPlacesHint {

  private final int decimalPlaces;

  public DecimalPlacesHint(int decimalPlaces) {
    this.decimalPlaces = decimalPlaces;
  }

  public int getDecimalPlaces() {
    return decimalPlaces;
  }
}
