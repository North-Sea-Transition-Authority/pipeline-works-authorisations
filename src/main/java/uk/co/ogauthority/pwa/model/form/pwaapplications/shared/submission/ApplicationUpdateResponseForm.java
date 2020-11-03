package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.submission;

public class ApplicationUpdateResponseForm {

  private Boolean madeOnlyRequestedChanges;

  private String otherChangesDescription;

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
}

