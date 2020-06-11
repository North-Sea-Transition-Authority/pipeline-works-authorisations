package uk.co.ogauthority.pwa.model.enums.notify;

/**
 * Enumeration of templates stored in GOV.UK Notify.
 */
public enum NotifyTemplate {

  CASE_OFFICER_ASSIGNED("CASE_OFFICER_ASSIGNED_V1");

  private final String templateName;

  NotifyTemplate(String templateName) {
    this.templateName = templateName;
  }

  public String getTemplateName() {
    return this.templateName;
  }
}
