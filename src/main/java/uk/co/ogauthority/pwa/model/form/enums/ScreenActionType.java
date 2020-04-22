package uk.co.ogauthority.pwa.model.form.enums;

public enum ScreenActionType {

  ADD("Add", "Add"),
  EDIT("Edit", "Update");

  private String actionText;
  private String submitButtonText;

  ScreenActionType(String actionText, String submitButtonText) {
    this.actionText = actionText;
    this.submitButtonText = submitButtonText;
  }

  public String getActionText() {
    return actionText;
  }

  public String getSubmitButtonText() {
    return submitButtonText;
  }
}
