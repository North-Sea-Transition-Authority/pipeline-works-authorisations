package uk.co.ogauthority.pwa.model.tasklist;

import java.util.List;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskInfo;

public class TaskListEntry {

  private String taskName;
  private String route;
  private boolean completed;
  private final TaskInfo taskInfo;
  private List<TaskListLabel> labels;

  public TaskListEntry(String taskName, String route, boolean completed) {
    this.taskName = taskName;
    this.route = route;
    this.completed = completed;
    this.taskInfo = null;
    this.labels = List.of();
  }

  public TaskListEntry(String taskName, String route, boolean completed,
                       TaskInfo taskInfo) {
    this.taskName = taskName;
    this.route = route;
    this.completed = completed;
    this.taskInfo = taskInfo;
//    this.labels = labels;
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

  public List<TaskListLabel> getLabels() {
    return labels;
  }
}
