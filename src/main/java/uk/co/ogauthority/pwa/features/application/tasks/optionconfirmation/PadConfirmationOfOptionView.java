package uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation;

public class PadConfirmationOfOptionView {

  private final String workType;
  private final String workDescription;

  PadConfirmationOfOptionView(String workType, String workDescription) {
    this.workType = workType;
    this.workDescription = workDescription;
  }

  public String getWorkType() {
    return workType;
  }

  public String getWorkDescription() {
    return workDescription;
  }
}
