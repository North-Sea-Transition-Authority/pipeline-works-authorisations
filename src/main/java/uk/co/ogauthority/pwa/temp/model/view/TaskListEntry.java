package uk.co.ogauthority.pwa.temp.model.view;

public class TaskListEntry {

  private String taskName;
  private String taskRoute;
  private boolean completed;

  public TaskListEntry(String taskName, String taskRoute, boolean completed) {
    this.taskName = taskName;
    this.taskRoute = taskRoute;
    this.completed = completed;
  }

  public String getTaskName() {
    return taskName;
  }

  public String getTaskRoute() {
    return taskRoute;
  }

  public boolean isCompleted() {
    return completed;
  }
}
