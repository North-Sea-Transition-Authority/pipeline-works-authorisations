package uk.co.ogauthority.pwa.features.generalcase.tasklist;

import java.util.List;

/**
 * A group of tasks which are within a single logical grouping.
 */
public class TaskListGroup {

  private final String groupName;
  private final int displayOrder;
  private final List<TaskListEntry> taskListEntries;

  public TaskListGroup(String groupName,
                       int displayOrder,
                       List<TaskListEntry> taskListEntries) {
    this.groupName = groupName;
    this.displayOrder = displayOrder;
    this.taskListEntries = taskListEntries;
  }

  public String getGroupName() {
    return groupName;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public List<TaskListEntry> getTaskListEntries() {
    return taskListEntries;
  }
}
