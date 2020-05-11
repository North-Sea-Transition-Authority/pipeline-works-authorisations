package uk.co.ogauthority.pwa.model.tasklist;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Provides common functionality for a service to implement that provides information about the section.
 */
public interface TaskListSection {

  /**
   * Used to display the complete label.
   * @return True if complete
   */
  boolean isTaskListEntryCompleted(PwaApplicationDetail pwaApplicationDetail);

  /**
   * Used to show/hide the task list entry.
   * @return True if entry should be shown.
   */
  boolean getCanShowInTaskList(PwaApplicationDetail pwaApplicationDetail);

  boolean getShowNotCompletedLabel();

  /**
   * Use to retrieve a list of extra labels to add to the task list entry.
   * @return List of TaskListLabels.
   */
  List<TaskListLabel> getTaskListLabels(PwaApplicationDetail pwaApplicationDetail);

}
