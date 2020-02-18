package uk.co.ogauthority.pwa.temp.model.locations;

public enum MedianLineSelection {

  NOT_CROSSED("Not crossed"),
  IN_DISCUSSION("In discussion"),
  AGREED("Requirements agreed");

  private String displayText;

  MedianLineSelection(String displayText) {
    this.displayText = displayText;
  }

  @Override
  public String toString() {
    return displayText;
  }

}
