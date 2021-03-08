package uk.co.ogauthority.pwa.model.enums.tasklist;

/**
 * Enumeration of available states for app/processing task lists.
 */
public enum TaskState {

  /**
   * Can access, view and edit task data.
   */
  EDIT,

  /**
   * Can access but only view task data.
   */
  VIEW,

  /**
   * Can't access task at all, even to view.
   */
  LOCK

}
