package uk.co.ogauthority.pwa.model.form.pwaapplications.start;

import javax.validation.constraints.NotNull;

public class StartPwaApplicationForm {

  @NotNull(message = "Select an application type")
  private String applicationType;

  public StartPwaApplicationForm() {

  }

  public String getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(String applicationType) {
    this.applicationType = applicationType;
  }

}
