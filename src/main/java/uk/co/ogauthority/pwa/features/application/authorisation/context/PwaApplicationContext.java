package uk.co.ogauthority.pwa.features.application.authorisation.context;

import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * A data class to store contextual information for a PWA application to allow for easier access to commonly required data.
 */
public class PwaApplicationContext {

  private final PwaApplicationDetail applicationDetail;

  private final WebUserAccount user;
  private final Set<PwaApplicationPermission> permissions;

  private PadPipeline padPipeline;
  private PadFile padFile;

  public PwaApplicationContext(PwaApplicationDetail applicationDetail,
                               WebUserAccount user,
                               Set<PwaApplicationPermission> permissions) {
    this.applicationDetail = applicationDetail;
    this.user = user;
    this.permissions = permissions;
  }

  public boolean hasPermission(PwaApplicationPermission pwaApplicationPermission) {
    return permissions.contains(pwaApplicationPermission);
  }

  public PwaApplicationDetail getApplicationDetail() {
    return applicationDetail;
  }

  public WebUserAccount getUser() {
    return user;
  }

  public Set<PwaApplicationPermission> getPermissions() {
    return permissions;
  }

  public PwaApplication getPwaApplication() {
    return applicationDetail.getPwaApplication();
  }

  public PwaApplicationType getApplicationType() {
    return applicationDetail.getPwaApplicationType();
  }

  public void setPadPipeline(PadPipeline padPipeline) {
    this.padPipeline = padPipeline;
  }

  public PadPipeline getPadPipeline() {
    return padPipeline;
  }

  public PadFile getPadFile() {
    return padFile;
  }

  public void setPadFile(PadFile padFile) {
    this.padFile = padFile;
  }

  public int getMasterPwaApplicationId() {
    return this.applicationDetail.getMasterPwaApplicationId();
  }

  @Override
  public String toString() {
    return "PwaApplicationContext{" +
        "applicationDetail=" + applicationDetail +
        ", user=" + user +
        ", permissions=" + permissions +
        ", padPipeline=" + padPipeline +
        ", padFile=" + padFile +
        '}';
  }
}
