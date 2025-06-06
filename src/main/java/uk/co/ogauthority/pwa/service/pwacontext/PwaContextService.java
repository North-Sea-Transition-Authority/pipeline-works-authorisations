package uk.co.ogauthority.pwa.service.pwacontext;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchService;

@Service
public class PwaContextService {

  private final PwaPermissionService pwaPermissionService;
  private final MasterPwaService masterPwaService;
  private final ConsentSearchService consentSearchService;
  private final PipelineService pipelineService;

  @Autowired
  public PwaContextService(PwaPermissionService pwaPermissionService,
                           MasterPwaService masterPwaService,
                           ConsentSearchService consentSearchService,
                           PipelineService pipelineService) {
    this.pwaPermissionService = pwaPermissionService;
    this.masterPwaService = masterPwaService;
    this.consentSearchService = consentSearchService;
    this.pipelineService = pipelineService;
  }

  /**
   * Construct an pwa context to provide common objects associated with a PWA and perform
   * standard permission checks.
   * @return pwa context if app is in right state and user has right permissions, throw relevant exceptions otherwise
   */
  public PwaContext validateAndCreate(PwaContextParams contextParams) {

    var pwaId = contextParams.getPwaId();
    var context = getPwaContext(pwaId, contextParams.getAuthenticatedUserAccount());

    performPermissionCheck(
        contextParams.getRequiredPwaPermissions(),
        context.getPwaPermissions(),
        contextParams.getAuthenticatedUserAccount(),
        pwaId);

    if (contextParams.getPipelineId() != null) {
      getAndSetPipeline(context, contextParams.getPipelineId());
    }

    return context;

  }

  /**
   * Construct a pwa context to provide common objects associated with a master Pwa.
   * @param pwaId for the Master Pwa
   * @param authenticatedUser trying to access the master PWA
   * @return pwa context object with pwa view, users permissions etc populated
   */
  @VisibleForTesting
  PwaContext getPwaContext(Integer pwaId,
                           AuthenticatedUserAccount authenticatedUser) {

    var masterPwa = masterPwaService.getMasterPwaById(pwaId);
    var pwaPermissions = pwaPermissionService.getPwaPermissions(masterPwa, authenticatedUser);

    if (pwaPermissions.isEmpty()) {
      throw new AccessDeniedException(
          String.format("User with WUA ID: %s has no pwa permissions", authenticatedUser.getWuaId()));
    }

    return new PwaContext(
        masterPwa,
        authenticatedUser,
        pwaPermissions,
        consentSearchService.getConsentSearchResultView(pwaId));

  }

  /**
   * If the user has ANY of the required permissions then pass, otherwise throw a relevant exception.
   */
  private void performPermissionCheck(Set<PwaPermission> requiredPermissions,
                                      Set<PwaPermission> usersPermissions,
                                      AuthenticatedUserAccount user,
                                      int pwaId) {

    if (!requiredPermissions.isEmpty()) {

      boolean userHasRequiredPermissions = requiredPermissions.stream()
          .anyMatch(usersPermissions::contains);

      if (!userHasRequiredPermissions) {
        throwPermissionException(user.getWuaId(), pwaId, requiredPermissions);
      }

    }

  }

  private void throwPermissionException(int wuaId,
                                        int pwaId,
                                        Set<PwaPermission> requiredPermissions) {
    throw new AccessDeniedException(
        String.format(
            "User with wua ID: %s can't access the PWA with ID: %s as they do not have the required permissions: %s",
            wuaId,
            pwaId,
            requiredPermissions
        )
    );
  }


  /**
   * If a pipeline is found for the requested ID (and it's on the same master pwa as the context), then add to the context.
   * Otherwise throw a relevant exception.
   */
  private void getAndSetPipeline(PwaContext context, int pipelineId) {

    var pipeline = pipelineService.getPipelineFromId(new PipelineId(pipelineId));

    if (!Objects.equals(pipeline.getMasterPwa(), context.getMasterPwa())) {
      throw new AccessDeniedException(String.format("Pipeline master pwa (%s) didn't match the app context's master pwa (%s)",
          pipeline.getMasterPwa().getId(),
          context.getMasterPwa().getId()));
    }

    context.setPipeline(pipeline);
  }


}
