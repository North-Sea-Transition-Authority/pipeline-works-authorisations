package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import javax.validation.constraints.NotNull;

public class PickPwaForm {
  @NotNull(message = "Select a PWA")
  private Integer masterPwaId;

  public Integer getMasterPwaId() {
    return masterPwaId;
  }

  public void setMasterPwaId(Integer masterPwaId) {
    this.masterPwaId = masterPwaId;
  }
}
