package uk.co.ogauthority.pwa.model.form.appprocessing.options;

public class ChangeOptionsApprovalDeadlineForm {

  private Integer deadlineDateDay;
  private Integer deadlineDateMonth;
  private Integer deadlineDateYear;

  private String note;

  public Integer getDeadlineDateDay() {
    return deadlineDateDay;
  }

  public void setDeadlineDateDay(Integer deadlineDateDay) {
    this.deadlineDateDay = deadlineDateDay;
  }

  public Integer getDeadlineDateMonth() {
    return deadlineDateMonth;
  }

  public void setDeadlineDateMonth(Integer deadlineDateMonth) {
    this.deadlineDateMonth = deadlineDateMonth;
  }

  public Integer getDeadlineDateYear() {
    return deadlineDateYear;
  }

  public void setDeadlineDateYear(Integer deadlineDateYear) {
    this.deadlineDateYear = deadlineDateYear;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
