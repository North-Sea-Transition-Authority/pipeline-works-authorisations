package uk.co.ogauthority.pwa.service.pwaapplications.context;

import java.util.Set;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

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

}
