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

  private Set<PwaApplicationStatus> statuses;
  private Set<PwaAppProcessingPermission> appProcessingPermissions;

  private String fileId;

  public PwaAppProcessingContextParams(int applicationId, AuthenticatedUserAccount authenticatedUserAccount) {
    this.applicationId = applicationId;
    this.authenticatedUserAccount = authenticatedUserAccount;
    this.statuses = Set.of();
    this.appProcessingPermissions = Set.of();
  }

  public PwaAppProcessingContextParams requiredAppStatuses(Set<PwaApplicationStatus> statuses) {
    this.statuses = statuses;
    return this;
  }

  public PwaAppProcessingContextParams requiredProcessingPermissions(Set<PwaAppProcessingPermission> appProcessingPermissions) {
    this.appProcessingPermissions = appProcessingPermissions;
    return this;
  }

  public PwaAppProcessingContextParams withFileId(String fileId) {
    this.fileId = fileId;
    return this;
  }

  public int getApplicationId() {
    return applicationId;
  }

  public AuthenticatedUserAccount getAuthenticatedUserAccount() {
    return authenticatedUserAccount;
  }

  public Set<PwaApplicationStatus> getStatuses() {
    return statuses;
  }

  public Set<PwaAppProcessingPermission> getAppProcessingPermissions() {
    return appProcessingPermissions;
  }

  public String getFileId() {
    return fileId;
  }

}
