package uk.co.ogauthority.pwa.service.enums.appprocessing;

/**
 * Whether or not it is possible to automatically lock a task based on conditions in
 * {@link uk.co.ogauthority.pwa.service.appprocessing.tasks.PwaAppProcessingTaskListService}.
 * If NO, the task's own lock rules will apply in such cases.
 */
public enum TaskAutoLockable {

  YES, NO

}
