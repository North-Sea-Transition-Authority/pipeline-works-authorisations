package uk.co.ogauthority.pwa.features.generalcase.tasklist;

import java.util.List;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;

/**
 * A single task within a task list.
 */
public class TaskListEntry {

  private final String taskName;
  private final String route;
  private final boolean completed;

  private final TaskTag taskTag;

  private final List<TaskInfo> taskInfoList;
  private final int displayOrder;

  private TaskState taskState;

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
    this.taskTag = null;
    this.taskInfoList = taskInfoList;
    this.displayOrder = displayOrder;
    this.taskState = TaskState.EDIT;
  }

  public TaskListEntry(String taskName,
                       String route,
                       TaskTag taskTag,
                       int displayOrder) {
    this(taskName, route, taskTag, List.of(), displayOrder);
  }

  public TaskListEntry(String taskName,
                       String route,
                       TaskTag taskTag,
                       List<TaskInfo> taskInfoList,
                       int displayOrder) {
    this.taskName = taskName;
    this.route = route;
    this.completed = false;
    this.taskTag = taskTag;
    this.taskInfoList = taskInfoList;
    this.displayOrder = displayOrder;
    this.taskState = TaskState.EDIT;
  }

  public TaskListEntry(String taskName,
                       String route,
                       TaskTag taskTag,
                       TaskState taskState,
                       int displayOrder) {
    this.taskName = taskName;
    this.route = route;
    this.completed = false;
    this.taskTag = taskTag;
    this.taskInfoList = List.of();
    this.displayOrder = displayOrder;
    this.taskState = taskState;
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

  public TaskTag getTaskTag() {
    return taskTag;
  }

  public List<TaskInfo> getTaskInfoList() {
    return taskInfoList;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public TaskState getTaskState() {
    return taskState;
  }

  public void setTaskState(TaskState taskState) {
    this.taskState = taskState;
  }

}
