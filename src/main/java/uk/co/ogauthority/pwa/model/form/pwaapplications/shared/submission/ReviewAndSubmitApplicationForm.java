package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.submission;

public class ReviewAndSubmitApplicationForm {

  // app update fields
  private Boolean madeOnlyRequestedChanges;
  private String otherChangesDescription;

  private Integer submitterPersonId;

  public Boolean getMadeOnlyRequestedChanges() {
    return madeOnlyRequestedChanges;
  }

  public void setMadeOnlyRequestedChanges(Boolean madeOnlyRequestedChanges) {
    this.madeOnlyRequestedChanges = madeOnlyRequestedChanges;
  }

  public String getOtherChangesDescription() {
    return otherChangesDescription;
  }

  public void setOtherChangesDescription(String otherChangesDescription) {
    this.otherChangesDescription = otherChangesDescription;
  }

  public Integer getSubmitterPersonId() {
    return submitterPersonId;
  }

  public void setSubmitterPersonId(Integer submitterPersonId) {
    this.submitterPersonId = submitterPersonId;
  }

}

