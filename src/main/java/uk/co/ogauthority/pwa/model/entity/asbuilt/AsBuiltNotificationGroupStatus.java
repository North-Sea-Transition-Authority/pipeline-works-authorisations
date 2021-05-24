package uk.co.ogauthority.pwa.model.entity.asbuilt;

/**
 * Describes the current state of an as built notification group.
 */
public enum AsBuiltNotificationGroupStatus {

  NOT_STARTED("Not started"), // no pipeline notifications submitted
  IN_PROGRESS("In progress"), // some pipeline notifications submitted
  COMPLETE("Complete"); // all pipeline notifications submitted;

  private final String displayName;

  AsBuiltNotificationGroupStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

}
