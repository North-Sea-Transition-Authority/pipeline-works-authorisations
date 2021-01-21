package uk.co.ogauthority.pwa.service.search.applicationsearch;

public class ApplicationSearchParametersBuilder {
  private String appReference;

  public ApplicationSearchParametersBuilder setAppReference(String appReference) {
    this.appReference = appReference;
    return this;
  }

  public ApplicationSearchParameters createApplicationSearchParameters() {
    return new ApplicationSearchParameters(appReference);
  }

  public static ApplicationSearchParameters createEmptyParams() {
    return new ApplicationSearchParameters(null);
  }
}