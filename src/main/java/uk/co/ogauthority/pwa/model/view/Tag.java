package uk.co.ogauthority.pwa.model.view;

public enum Tag {

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
