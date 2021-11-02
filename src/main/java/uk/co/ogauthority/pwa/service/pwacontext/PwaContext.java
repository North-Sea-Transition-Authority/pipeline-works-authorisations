package uk.co.ogauthority.pwa.service.pwacontext;

import java.util.Set;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;

/**
 * A data class to store contextual information related to a PWA to allow
 * for easier access to commonly required data.
 */
public class PwaContext {

  private final MasterPwa masterPwa;
  private final WebUserAccount user;
  private final Set<PwaPermission> pwaPermissions;
  private final ConsentSearchResultView consentSearchResultView;
  private Pipeline pipeline;

  public PwaContext(MasterPwa masterPwa, WebUserAccount user,
                    Set<PwaPermission> pwaPermissions,
                    ConsentSearchResultView consentSearchResultView) {
    this.masterPwa = masterPwa;
    this.user = user;
    this.pwaPermissions = pwaPermissions;
    this.consentSearchResultView = consentSearchResultView;
  }

  public MasterPwa getMasterPwa() {
    return masterPwa;
  }

  public WebUserAccount getUser() {
    return user;
  }

  public Set<PwaPermission> getPwaPermissions() {
    return pwaPermissions;
  }

  public ConsentSearchResultView getConsentSearchResultView() {
    return consentSearchResultView;
  }

  public Pipeline getPipeline() {
    return pipeline;
  }

  public void setPipeline(Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  public boolean hasPermission(PwaPermission pwaPermission) {
    return pwaPermissions.contains(pwaPermission);
  }


}
