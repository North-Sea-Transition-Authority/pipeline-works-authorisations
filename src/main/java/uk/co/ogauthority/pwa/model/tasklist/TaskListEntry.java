package uk.co.ogauthority.pwa.model.tasklist;

public class TaskListEntry {

  private String taskName;
  private String route;
  private boolean completed;

  public TaskListEntry(String taskName, String route, boolean completed) {
    this.taskName = taskName;
    this.route = route;
    this.completed = completed;
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
}
