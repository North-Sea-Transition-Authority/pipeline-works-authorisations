package uk.co.ogauthority.pwa.features.application.authorisation.context;

import java.util.Set;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

/**
 * Builder class to set up requirements and checks for a {@link PwaApplicationContext}.
 * Create the context with the {@link PwaApplicationContextService}.
 */
public class PwaApplicationContextParams {

  private final int applicationId;
  private final AuthenticatedUserAccount authenticatedUserAccount;

  private Set<PwaApplicationStatus> statuses;
  private Set<PwaApplicationType> types;
  private Set<PwaApplicationPermission> permissions;

  private Integer padPipelineId;
  private String fileId;

  public PwaApplicationContextParams(int applicationId, AuthenticatedUserAccount authenticatedUserAccount) {
    this.applicationId = applicationId;
    this.authenticatedUserAccount = authenticatedUserAccount;
    statuses = Set.of();
    types = Set.of();
    permissions = Set.of();
  }

  public PwaApplicationContextParams requiredAppStatuses(Set<PwaApplicationStatus> statuses) {
    this.statuses = statuses;
    return this;
  }

  public PwaApplicationContextParams requiredAppTypes(Set<PwaApplicationType> types) {
    this.types = types;
    return this;
  }

  public PwaApplicationContextParams requiredUserPermissions(Set<PwaApplicationPermission> permissions) {
    this.permissions = permissions;
    return this;
  }

  public PwaApplicationContextParams withPadPipelineId(Integer padPipelineId) {
    this.padPipelineId = padPipelineId;
    return this;
  }

  public PwaApplicationContextParams withFileId(String fileId) {
    this.fileId = fileId;
    return this;
  }

  int getApplicationId() {
    return applicationId;
  }

  AuthenticatedUserAccount getAuthenticatedUserAccount() {
    return authenticatedUserAccount;
  }

  Set<PwaApplicationStatus> getStatuses() {
    return statuses;
  }

  Set<PwaApplicationType> getTypes() {
    return types;
  }

  public Set<PwaApplicationPermission> getPermissions() {
    return permissions;
  }

  Integer getPadPipelineId() {
    return padPipelineId;
  }

  public String getFileId() {
    return fileId;
  }

}
