package uk.co.ogauthority.pwa.model.enums.notify;

public enum NotifyTemplateType {

  EMAIL_TEMPLATE_TYPE("email");

  private final String typeName;

  NotifyTemplateType(String typeName) {
    this.typeName = typeName;
  }

  public String getTypeName() {
    return typeName;
  }
}
