package uk.co.ogauthority.pwa.model.tasklist;

public class TaskListLabel {

  private String displayText;
  private TagColour colour;

  public TaskListLabel(String displayText, TagColour colour) {
    this.displayText = displayText;
    this.colour = colour;
  }

  public String getDisplayText() {
    return displayText;
  }

  public TagColour getColour() {
    return colour;
  }
}
