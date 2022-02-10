package uk.co.ogauthority.pwa.features.appprocessing.tasklist;

/**
 * Whether or not it is possible to automatically lock a task based on conditions in
 * {@link PwaAppProcessingTaskListService}.
 * If NO, the task's own lock rules will apply in such cases.
 */
public enum TaskAutoLockable {

  YES, NO

}
