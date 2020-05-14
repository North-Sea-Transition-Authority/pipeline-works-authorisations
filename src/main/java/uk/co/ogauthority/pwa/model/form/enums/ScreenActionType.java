package uk.co.ogauthority.pwa.model.form.enums;

import java.util.Arrays;
import java.util.stream.Stream;

public enum ScreenActionType {

  ADD("Add", "Add"),
  EDIT("Edit", "Update");

  private final String actionText;
  private final String submitButtonText;

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

  public static Stream<ScreenActionType> stream() {
    return Arrays.stream(ScreenActionType.values());
  }
}
