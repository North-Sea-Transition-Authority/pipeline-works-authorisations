package uk.co.ogauthority.pwa.service.pwaapplications.context;

import java.util.Set;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

/**
 * A data class to store contextual information for a PWA application to allow for easier access to commonly required data.
 */
public class PwaApplicationContext {

  private PwaApplicationDetail applicationDetail;

  private final WebUserAccount user;
  private Set<PwaContactRole> userRoles;

  public PwaApplicationContext(PwaApplicationDetail applicationDetail,
                               WebUserAccount user,
                               Set<PwaContactRole> userRoles) {
    this.applicationDetail = applicationDetail;
    this.user = user;
    this.userRoles = userRoles;
  }

  public PwaApplicationDetail getApplicationDetail() {
    return applicationDetail;
  }

  public WebUserAccount getUser() {
    return user;
  }

  public Set<PwaContactRole> getUserRoles() {
    return userRoles;
  }

  public PwaApplication getPwaApplication() {
    return applicationDetail.getPwaApplication();
  }
}
