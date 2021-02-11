package uk.co.ogauthority.pwa.service.search.applicationsearch;

import java.util.Objects;

public final class ApplicationSearchParameters {

  private String appReference;

  private Boolean includeCompletedOrWithdrawnApps;

  private String caseOfficerId;

  public ApplicationSearchParameters(String appReference, Boolean includeCompletedOrWithdrawnApps,
                                     String caseOfficerId) {
    this.appReference = appReference;
    this.includeCompletedOrWithdrawnApps = includeCompletedOrWithdrawnApps;
    this.caseOfficerId = caseOfficerId;
  }

  public String getAppReference() {
    return appReference;
  }

  public void setAppReference(String appReference) {
    this.appReference = appReference;
  }

  public Boolean getIncludeCompletedOrWithdrawnApps() {
    return includeCompletedOrWithdrawnApps;
  }

  public void setIncludeCompletedOrWithdrawnApps(Boolean includeCompletedOrWithdrawnApps) {
    this.includeCompletedOrWithdrawnApps = includeCompletedOrWithdrawnApps;
  }

  public String getCaseOfficerId() {
    return caseOfficerId;
  }

  public void setCaseOfficerId(String caseOfficerId) {
    this.caseOfficerId = caseOfficerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationSearchParameters that = (ApplicationSearchParameters) o;
    return Objects.equals(appReference, that.appReference)
        && Objects.equals(includeCompletedOrWithdrawnApps, that.includeCompletedOrWithdrawnApps)
        && Objects.equals(caseOfficerId, that.caseOfficerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(appReference, includeCompletedOrWithdrawnApps, caseOfficerId);
  }

  @Override
  public String toString() {
    return "ApplicationSearchParameters{" +
        "appReference='" + appReference + '\'' +
        ", includeCompletedOrWithdrawnApps=" + includeCompletedOrWithdrawnApps + '\'' +
        ", caseOfficerId=" + caseOfficerId +
        '}';
  }
}
