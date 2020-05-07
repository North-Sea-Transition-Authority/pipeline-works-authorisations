package uk.co.ogauthority.pwa.model.tasklist;

import java.util.List;

public class TaskListEntry {

  private String taskName;
  private String route;
  private boolean completed;
  private List<TaskListLabel> labels;

  public TaskListEntry(String taskName, String route, boolean completed) {
    this.taskName = taskName;
    this.route = route;
    this.completed = completed;
    this.labels = List.of();
  }

  public TaskListEntry(String taskName, String route, boolean completed, List<TaskListLabel> labels) {
    this.taskName = taskName;
    this.route = route;
    this.completed = completed;
    this.labels = labels;
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

  public List<TaskListLabel> getLabels() {
    return labels;
  }
}
