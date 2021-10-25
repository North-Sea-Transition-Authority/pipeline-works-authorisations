package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

/*
 * Describes whether a structure at one end of a pipeline ident is included in a pipeline segment.
 */
public enum IdentLocationInclusionMode {

  INCLUSIVE("including"), EXCLUSIVE("not including");

  private static final String FROM_LOCATION_FORMAT = "from and %s %s";
  private static final String TO_LOCATION_FORMAT = "to and %s %s";

  private final String displayString;

  IdentLocationInclusionMode(String displayString) {
    this.displayString = displayString;
  }

  public String getFromLocationDisplayString(String location) {
    return String.format(FROM_LOCATION_FORMAT, this.displayString, location);
  }

  public String getToLocationDisplayString(String location) {
    return String.format(TO_LOCATION_FORMAT, this.displayString, location);
  }
}
