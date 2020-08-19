package uk.co.ogauthority.pwa.model.enums.notify;

/**
 * Enumeration of templates stored in GOV.UK Notify.
 */
public enum NotifyTemplate {

  // Application workflow changes
  APPLICATION_SUBMITTED("APPLICATION_SUBMITTED_V1"),

  // Assignment
  CASE_OFFICER_ASSIGNED("CASE_OFFICER_ASSIGNED_V1"),
  CONSULTATION_ASSIGNED_TO_YOU("CONSULTATION_ASSIGNED_TO_YOU_V1"),

  //Consultation withdrawn
  CONSULTATION_WITHDRAWN("CONSULTATION_WITHDRAWN_V1"),

  // Notify callbacks
  EMAIL_DELIVERY_FAILED("EMAIL_DELIVERY_FAILED_V1");

  private final String templateName;

  NotifyTemplate(String templateName) {
    this.templateName = templateName;
  }

  public String getTemplateName() {
    return this.templateName;
  }
}
