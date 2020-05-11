package uk.co.ogauthority.pwa.model.tasklist;

public enum TagColour {

  RED("red"),
  ORANGE("orange"),
  BROWN("brown"),
  GREY("grey"),
  INACTIVE("inactive"),
  BLUE("blue");

  private String cssName;

  TagColour(String cssName) {
    this.cssName = cssName;
  }

  public String getCssName() {
    return cssName;
  }
}
