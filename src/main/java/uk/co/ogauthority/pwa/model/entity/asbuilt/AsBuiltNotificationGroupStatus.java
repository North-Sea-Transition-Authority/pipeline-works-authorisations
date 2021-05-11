package uk.co.ogauthority.pwa.model.entity.asbuilt;

/**
 * Describes the current state of an as built notification group.
 */
public enum AsBuiltNotificationGroupStatus {

  NOT_STARTED, // no pipeline notifications submitted
  IN_PROGRESS, // some pipeline notifications submitted
  COMPLETE // all pipeline notifications submitted
}
