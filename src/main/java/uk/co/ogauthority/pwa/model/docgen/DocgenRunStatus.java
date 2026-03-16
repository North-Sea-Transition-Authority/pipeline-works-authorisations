package uk.co.ogauthority.pwa.model.docgen;

import uk.co.ogauthority.pwa.util.enumutils.Displayable;

public enum DocgenRunStatus implements Displayable {

  PENDING("Pending"),
  COMPLETE("Completed"),
  FAILED("Failed");

  private final String displayName;

  DocgenRunStatus(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

}
