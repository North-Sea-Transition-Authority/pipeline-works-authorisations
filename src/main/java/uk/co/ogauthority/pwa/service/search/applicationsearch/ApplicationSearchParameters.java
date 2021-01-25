package uk.co.ogauthority.pwa.service.search.applicationsearch;

import java.util.Objects;

public final class ApplicationSearchParameters {

  private String appReference;

  public ApplicationSearchParameters(String appReference) {
    this.appReference = appReference;
  }

  public String getAppReference() {
    return appReference;
  }

  public void setAppReference(String appReference) {
    this.appReference = appReference;
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
    return Objects.equals(appReference, that.appReference);
  }

  @Override
  public int hashCode() {
    return Objects.hash(appReference);
  }

  @Override
  public String toString() {
    return "ApplicationSearchParameters{" +
        "appReference='" + appReference + '\'' +
        '}';
  }
}
