package uk.co.ogauthority.pwa.service.pwacontext;

import java.util.Set;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;

/**
 * Builder class to set up requirements and checks for a {@link PwaContext}.
 * Create the context with the {@link PwaContextService}.
 */
public class PwaContextParams {

  private final int pwaId;
  private final AuthenticatedUserAccount authenticatedUserAccount;
  private Set<PwaPermission> requiredPwaPermissions;
  private Integer pipelineId;

  public PwaContextParams(int pwaId, AuthenticatedUserAccount authenticatedUserAccount) {
    this.pwaId = pwaId;
    this.authenticatedUserAccount = authenticatedUserAccount;
    this.requiredPwaPermissions = Set.of();
  }


  public PwaContextParams requiredPwaPermissions(Set<PwaPermission> pwaPermissions) {
    this.requiredPwaPermissions = pwaPermissions;
    return this;
  }

  public PwaContextParams withPipelineId(Integer pipelineId) {
    this.pipelineId = pipelineId;
    return this;
  }


  public int getPwaId() {
    return pwaId;
  }

  public AuthenticatedUserAccount getAuthenticatedUserAccount() {
    return authenticatedUserAccount;
  }

  public Set<PwaPermission> getRequiredPwaPermissions() {
    return requiredPwaPermissions;
  }

  public Integer getPipelineId() {
    return pipelineId;
  }
}
