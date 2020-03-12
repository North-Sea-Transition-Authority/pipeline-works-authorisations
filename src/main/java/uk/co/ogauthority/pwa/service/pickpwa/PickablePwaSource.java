package uk.co.ogauthority.pwa.service.pickpwa;
/**
 * When picking a PWA, is it one which is already migrated or create by the new system or do we need to migrate it?
 */
public enum PickablePwaSource {
  MASTER("MASTER_PWA/"), MIGRATION("MIGRATION_PWA/"), UNKNOWN("");

  private final String pickableStringPrefix;

  PickablePwaSource(String pickableStringPrefix) {
    this.pickableStringPrefix = pickableStringPrefix;
  }

  public String getPickableStringPrefix() {
    return pickableStringPrefix;
  }
}
