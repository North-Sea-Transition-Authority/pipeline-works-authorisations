package uk.co.ogauthority.pwa.model.tasklist;

import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskInfo;

public class TaskListEntry {

  private final String taskName;
  private final String route;
  private final boolean completed;
  private final TaskInfo taskInfo;

  public TaskListEntry(String taskName, String route, boolean completed) {
    this.taskName = taskName;
    this.route = route;
    this.completed = completed;
    this.taskInfo = null;
  }

  public TaskListEntry(String taskName, String route, boolean completed,
                       TaskInfo taskInfo) {
    this.taskName = taskName;
    this.route = route;
    this.completed = completed;
    this.taskInfo = taskInfo;
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

  public TaskInfo getTaskInfo() {
    return taskInfo;
  }
}
