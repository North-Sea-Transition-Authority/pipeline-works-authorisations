package uk.co.ogauthority.pwa.service.pwaapplications.generic;

public class TaskInfo {

  private String link;
  private String countType;
  private Long count = 0L;

  public TaskInfo(String link) {
    this.link = link;
  }

  public TaskInfo(String link, String countType, Long count) {
    this.link = link;
    this.countType = countType;
    this.count = count;
  }

  public TaskInfo() {
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
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
