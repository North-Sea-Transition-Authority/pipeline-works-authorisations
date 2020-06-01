package uk.co.ogauthority.pwa.service.appprocessing.context;

import java.util.Set;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

/**
 * Builder class to set up requirements and checks for a {@link PwaAppProcessingContext}.
 * Create the context with the {@link PwaAppProcessingContextService}.
 */
public class PwaAppProcessingContextParams {

  private final int applicationId;
  private final AuthenticatedUserAccount authenticatedUserAccount;

  private PwaApplicationStatus status;
  private Set<PwaAppProcessingPermission> appProcessingPermissions;

  public PwaAppProcessingContextParams(int applicationId, AuthenticatedUserAccount authenticatedUserAccount) {
    this.applicationId = applicationId;
    this.authenticatedUserAccount = authenticatedUserAccount;
    this.appProcessingPermissions = Set.of();
  }

  public PwaAppProcessingContextParams requiredAppStatus(PwaApplicationStatus status) {
    this.status = status;
    return this;
  }

  public PwaAppProcessingContextParams requiredProcessingPermissions(Set<PwaAppProcessingPermission> appProcessingPermissions) {
    this.appProcessingPermissions = appProcessingPermissions;
    return this;
  }

  public int getApplicationId() {
    return applicationId;
  }

  public AuthenticatedUserAccount getAuthenticatedUserAccount() {
    return authenticatedUserAccount;
  }

  public PwaApplicationStatus getStatus() {
    return status;
  }

  public Set<PwaAppProcessingPermission> getAppProcessingPermissions() {
    return appProcessingPermissions;
  }

}
