package uk.co.ogauthority.pwa.model.tasklist;

import java.util.Objects;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;

public class TaskTag {

  private final String tagText;
  private final String tagClass;

  public TaskTag(String tagText, String tagClass) {
    this.tagText = tagText;
    this.tagClass = tagClass;
  }

  public static TaskTag from(TaskStatus taskStatus) {
    return new TaskTag(taskStatus.getDisplayText(), taskStatus.getTagClass());
  }

  public String getTagText() {
    return tagText;
  }

  public String getTagClass() {
    return tagClass;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaskTag taskTag = (TaskTag) o;
    return Objects.equals(tagText, taskTag.tagText)
        && Objects.equals(tagClass, taskTag.tagClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tagText, tagClass);
  }
}
