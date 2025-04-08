package uk.co.ogauthority.pwa.model.enums.notify;

public enum DomainType {
  APP("PWA_APPLICATION"),
  AS_BUILT_GROUP("AS_BUILT_NOTIFICATION_GROUP"),
  FAIL("FAILED_TO_SEND"),
  PAD("PWA_APPLICATION_DETAIL"),
  TEAM("TEAM");

  private final String typeName;

  DomainType(String typeName) {
    this.typeName = typeName;
  }

  public String getTypeName() {
    return typeName;
  }
}
