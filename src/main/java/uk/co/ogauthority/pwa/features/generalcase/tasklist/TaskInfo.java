package uk.co.ogauthority.pwa.features.generalcase.tasklist;

/**
 * This is a single piece of additional information about a single application task.
 */
public class TaskInfo {

  private String countType;
  private Long count;

  public TaskInfo(String countType, Long count) {
    this.countType = countType;
    this.count = count;
  }

  public String getCountType() {
    return countType;
  }

  public void setCountType(String countType) {
    this.countType = countType;
  }

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }

}
