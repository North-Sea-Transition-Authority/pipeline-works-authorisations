package uk.co.ogauthority.pwa.model.view.appsummary;

import java.util.Map;

public class VisibleApplicationVersionOptionsForUser {

  private final Map<String, String> applicationVersionOptions;

  public VisibleApplicationVersionOptionsForUser(Map<String, String> applicationVersionOptions) {
    this.applicationVersionOptions = applicationVersionOptions;
  }

  public Map<String, String> getApplicationVersionOptions() {
    return applicationVersionOptions;
  }

  public boolean isApplicationDetailPresent(Integer applicationDetailId) {
    return applicationVersionOptions.containsKey(applicationDetailId.toString());
  }

}
