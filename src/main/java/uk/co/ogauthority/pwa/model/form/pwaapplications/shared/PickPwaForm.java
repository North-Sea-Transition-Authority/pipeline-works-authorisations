package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import javax.validation.constraints.NotNull;

public class PickPwaForm {
  @NotNull(message = "Select a PWA")
  private String pickablePwaString;

  public String getPickablePwaString() {
    return pickablePwaString;
  }

  public void setPickablePwaString(String pickablePwaString) {
    this.pickablePwaString = pickablePwaString;
  }
}
