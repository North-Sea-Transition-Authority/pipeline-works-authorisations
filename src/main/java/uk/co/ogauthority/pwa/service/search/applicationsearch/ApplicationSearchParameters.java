package uk.co.ogauthority.pwa.service.search.applicationsearch;

import java.util.Objects;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public final class ApplicationSearchParameters {

  private String appReference;

  private Boolean includeCompletedOrWithdrawnApps;

  private Integer caseOfficerPersonId;

  private Integer holderOrgUnitId;

  private PwaApplicationType pwaApplicationType;

  public ApplicationSearchParameters(String appReference,
                                     Boolean includeCompletedOrWithdrawnApps,
                                     Integer caseOfficerPersonId,
                                     Integer holderOrgUnitId,
                                     PwaApplicationType pwaApplicationType) {
    this.appReference = appReference;
    this.includeCompletedOrWithdrawnApps = includeCompletedOrWithdrawnApps;
    this.caseOfficerPersonId = caseOfficerPersonId;
    this.holderOrgUnitId = holderOrgUnitId;
    this.pwaApplicationType = pwaApplicationType;
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

  public Integer getCaseOfficerPersonId() {
    return caseOfficerPersonId;
  }

  public void setCaseOfficerPersonId(Integer caseOfficerPersonId) {
    this.caseOfficerPersonId = caseOfficerPersonId;
  }

  public PwaApplicationType getPwaApplicationType() {
    return pwaApplicationType;
  }

  public void setPwaApplicationType(PwaApplicationType pwaApplicationType) {
    this.pwaApplicationType = pwaApplicationType;
  }

  public Integer getHolderOrgUnitId() {
    return holderOrgUnitId;
  }

  public void setHolderOrgUnitId(Integer holderOrgUnitId) {
    this.holderOrgUnitId = holderOrgUnitId;
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
        && Objects.equals(caseOfficerPersonId, that.caseOfficerPersonId)
        && Objects.equals(holderOrgUnitId, that.holderOrgUnitId)
        && pwaApplicationType == that.pwaApplicationType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(appReference, includeCompletedOrWithdrawnApps, caseOfficerPersonId, holderOrgUnitId,
        pwaApplicationType);
  }

  @Override
  public String toString() {
    return "ApplicationSearchParameters{" +
        "appReference='" + appReference + '\'' +
        ", includeCompletedOrWithdrawnApps=" + includeCompletedOrWithdrawnApps +
        ", caseOfficerId='" + caseOfficerPersonId + '\'' +
        ", holderOrgUnitId='" + holderOrgUnitId + '\'' +
        ", pwaApplicationType=" + pwaApplicationType +
        '}';
  }
}
