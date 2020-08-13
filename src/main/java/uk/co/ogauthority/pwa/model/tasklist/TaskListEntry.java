package uk.co.ogauthority.pwa.model.tasklist;

import java.util.List;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskInfo;

/**
 * A single task within a task list.
 */
public class TaskListEntry {

  private final String taskName;
  private final String route;
  private final boolean completed;
  private final List<TaskInfo> taskInfoList;
  private final int displayOrder;

  public TaskListEntry(String taskName,
                       String route,
                       boolean completed,
                       int displayOrder) {
    this(taskName, route, completed, List.of(), displayOrder);
  }

  public TaskListEntry(String taskName,
                       String route,
                       boolean completed,
                       List<TaskInfo> taskInfoList,
                       int displayOrder) {
    this.taskName = taskName;
    this.route = route;
    this.completed = completed;
    this.taskInfoList = taskInfoList;
    this.displayOrder = displayOrder;
  }

  public String getTaskName() {
    return taskName;
  }

  public String getRoute() {
    return route;
  }

  public boolean isCompleted() {
    return completed;
  }

  public List<TaskInfo> getTaskInfoList() {
    return taskInfoList;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }
}
