package uk.co.ogauthority.pwa.service.search.applicationsearch;

public class ApplicationSearchParametersBuilder {
  private String appReference;

  private Boolean includeCompletedOrWithdrawnApps;

  public ApplicationSearchParametersBuilder setAppReference(String appReference) {
    this.appReference = appReference;
    return this;
  }

  public ApplicationSearchParametersBuilder includeCompletedOrWithdrawnApps(Boolean includeCompletedOrWithdrawnApps) {
    this.includeCompletedOrWithdrawnApps = includeCompletedOrWithdrawnApps;
    return this;
  }

  public ApplicationSearchParameters createApplicationSearchParameters() {
    return new ApplicationSearchParameters(appReference, includeCompletedOrWithdrawnApps);
  }

  public static ApplicationSearchParameters createEmptyParams() {
    return new ApplicationSearchParameters(
        null, null
    );
  }
}