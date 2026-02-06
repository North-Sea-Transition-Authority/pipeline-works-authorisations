package uk.co.ogauthority.pwa.model.view;

import uk.co.ogauthority.pwa.util.enumutils.Displayable;

public enum Tag implements Displayable {

  NOT_FROM_PORTAL("NOT FROM PORTAL"),
  NONE("");

  private final String displayName;

  Tag(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
