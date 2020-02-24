package uk.co.ogauthority.pwa.temp.model.locations;

public enum MedianLineSelection {

  NOT_CROSSED("Not crossed"),
  NEGOTIATIONS_ONGOING("Negotiations ongoing"),
  NEGOTIATIONS_COMPLETE("Negotiations complete");

  private String displayText;

  MedianLineSelection(String displayText) {
    this.displayText = displayText;
  }

  @Override
  public String toString() {
    return displayText;
  }

}
