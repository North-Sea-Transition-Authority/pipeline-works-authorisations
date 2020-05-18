package uk.co.ogauthority.pwa.model.tasklist;

import java.util.List;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskInfo;

public class TaskListEntry {

  private String taskName;
  private String route;
  private boolean completed;
  private final List<TaskInfo> taskInfoList;

  public TaskListEntry(String taskName, String route, boolean completed) {
    this.taskName = taskName;
    this.route = route;
    this.completed = completed;
    this.taskInfoList = List.of();
  }

  public TaskListEntry(String taskName, String route, boolean completed,
                       List<TaskInfo> taskInfoList) {
    this.taskName = taskName;
    this.route = route;
    this.completed = completed;
    this.taskInfoList = taskInfoList;
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
}
