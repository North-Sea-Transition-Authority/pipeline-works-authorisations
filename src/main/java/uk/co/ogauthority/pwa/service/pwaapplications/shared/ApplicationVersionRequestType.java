package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import java.util.EnumSet;
import java.util.Set;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;

public enum ApplicationVersionRequestType {
  CURRENT_DRAFT(
      EnumSet.of(PwaAppProcessingPermission.UPDATE_APPLICATION),
      1, "In-progress application version"),
  LAST_SUBMITTED(
      EnumSet.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY, PwaAppProcessingPermission.CASE_MANAGEMENT_OGA),
      2, "Last submitted application version"),
  LAST_SATISFACTORY(
      EnumSet.of(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE),
      3, "Latest satisfactory version");

  private final Set<PwaAppProcessingPermission> accessibleByPermissions;
  private final int displayNum;
  private final String displayString;

  ApplicationVersionRequestType(
      Set<PwaAppProcessingPermission> accessibleByPermissions, int displayNum, String displayString) {
    this.accessibleByPermissions = accessibleByPermissions;
    this.displayNum = displayNum;
    this.displayString = displayString;
  }

  public Set<PwaAppProcessingPermission> getAccessibleByPermissions() {
    return accessibleByPermissions;
  }

  public int getDisplayNum() {
    return displayNum;
  }

  public String getDisplayString() {
    return displayString;
  }
}
