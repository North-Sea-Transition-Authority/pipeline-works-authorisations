package uk.co.ogauthority.pwa.model.form.enums;

/**
 * Used to modify behaviour on the pipeline add/update screen.
 */
public enum PipelineViewMode {

  UPDATE("Update"),
  NEW("New");

  private String displayText;

  PipelineViewMode(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

}
