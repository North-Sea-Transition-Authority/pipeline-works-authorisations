package uk.co.ogauthority.pwa.service.appprocessing.context;

import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * A data class to store contextual information related to processing a PWA application to allow
 * for easier access to commonly required data.
 */
public class PwaAppProcessingContext {

  private final PwaApplicationDetail applicationDetail;

  private final WebUserAccount user;
  private final Set<PwaAppProcessingPermission> appProcessingPermissions;

  private final CaseSummaryView caseSummaryView;

  public PwaAppProcessingContext(PwaApplicationDetail applicationDetail,
                                 WebUserAccount user,
                                 Set<PwaAppProcessingPermission> appProcessingPermissions,
                                 CaseSummaryView caseSummaryView) {
    this.applicationDetail = applicationDetail;
    this.user = user;
    this.appProcessingPermissions = appProcessingPermissions;
    this.caseSummaryView = caseSummaryView;
  }

  public PwaApplicationDetail getApplicationDetail() {
    return applicationDetail;
  }

  public WebUserAccount getUser() {
    return user;
  }

  public Set<PwaAppProcessingPermission> getAppProcessingPermissions() {
    return appProcessingPermissions;
  }

  public PwaApplication getPwaApplication() {
    return applicationDetail.getPwaApplication();
  }

  public PwaApplicationType getApplicationType() {
    return applicationDetail.getPwaApplicationType();
  }

  public CaseSummaryView getCaseSummaryView() {
    return caseSummaryView;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PwaAppProcessingContext that = (PwaAppProcessingContext) o;
    return Objects.equals(applicationDetail, that.applicationDetail)
        && Objects.equals(user, that.user)
        && Objects.equals(appProcessingPermissions, that.appProcessingPermissions)
        && Objects.equals(caseSummaryView, that.caseSummaryView);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationDetail, user, appProcessingPermissions, caseSummaryView);
  }
}
