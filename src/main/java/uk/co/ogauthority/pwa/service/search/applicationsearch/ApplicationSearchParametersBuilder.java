package uk.co.ogauthority.pwa.service.search.applicationsearch;

import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;

public class ApplicationSearchParametersBuilder {
  private String appReference;

  private Boolean includeCompletedOrWithdrawnApps;

  private Integer caseOfficerPersonId;

  private Integer holderOrgUnitId;

  private PwaApplicationType pwaApplicationType;

  public ApplicationSearchParametersBuilder setAppReference(String appReference) {
    this.appReference = appReference;
    return this;
  }

  public ApplicationSearchParametersBuilder includeCompletedOrWithdrawnApps(Boolean includeCompletedOrWithdrawnApps) {
    this.includeCompletedOrWithdrawnApps = includeCompletedOrWithdrawnApps;
    return this;
  }

  public ApplicationSearchParametersBuilder setCaseOfficerPersonId(Integer caseOfficerPersonId) {
    this.caseOfficerPersonId = caseOfficerPersonId;
    return this;
  }

  public ApplicationSearchParametersBuilder setHolderOrgUnitId(Integer holderOrgUnitId) {
    this.holderOrgUnitId = holderOrgUnitId;
    return this;
  }

  public ApplicationSearchParametersBuilder setPwaApplicationType(PwaApplicationType pwaApplicationType) {
    this.pwaApplicationType = pwaApplicationType;
    return this;
  }

  public ApplicationSearchParameters createApplicationSearchParameters() {
    return new ApplicationSearchParameters(
        appReference,
        includeCompletedOrWithdrawnApps,
        caseOfficerPersonId,
        holderOrgUnitId,
        pwaApplicationType
    );
  }

  public static ApplicationSearchParameters createEmptyParams() {
    return new ApplicationSearchParameters(
        null,
        null,
        null,
        null,
        null);
  }
}