package uk.co.ogauthority.pwa.service.search.applicationsearch;

import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class ApplicationSearchParametersBuilder {
  private String appReference;

  private Boolean includeCompletedOrWithdrawnApps;

  private String caseOfficerId;

  private PwaApplicationType pwaApplicationType;

  public ApplicationSearchParametersBuilder setAppReference(String appReference) {
    this.appReference = appReference;
    return this;
  }

  public ApplicationSearchParametersBuilder includeCompletedOrWithdrawnApps(Boolean includeCompletedOrWithdrawnApps) {
    this.includeCompletedOrWithdrawnApps = includeCompletedOrWithdrawnApps;
    return this;
  }

  public ApplicationSearchParametersBuilder setCaseOfficerId(String caseOfficerId) {
    this.caseOfficerId = caseOfficerId;
    return this;
  }

  public ApplicationSearchParametersBuilder setPwaApplicationType(PwaApplicationType pwaApplicationType) {
    this.pwaApplicationType = pwaApplicationType;
    return this;
  }

  public ApplicationSearchParameters createApplicationSearchParameters() {
    return new ApplicationSearchParameters(appReference, includeCompletedOrWithdrawnApps, caseOfficerId,
        pwaApplicationType);
  }

  public static ApplicationSearchParameters createEmptyParams() {
    return new ApplicationSearchParameters(
        null, null, null, null);
  }
}