package uk.co.ogauthority.pwa.service.pwacontext;

import java.util.Set;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;

/**
 * A data class to store contextual information related to a PWA to allow
 * for easier access to commonly required data.
 */
public class PwaContext {

  private final WebUserAccount user;
  private final Set<PwaPermission> pwaPermissions;
  private final ConsentSearchResultView consentSearchResultView;

  public PwaContext(WebUserAccount user,
                    Set<PwaPermission> pwaPermissions,
                    ConsentSearchResultView consentSearchResultView) {
    this.user = user;
    this.pwaPermissions = pwaPermissions;
    this.consentSearchResultView = consentSearchResultView;
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

  public boolean hasPermission(PwaPermission pwaPermission) {
    return pwaPermissions.contains(pwaPermission);
  }


}
