package uk.co.ogauthority.pwa.features.analytics;

public enum AnalyticsEventCategory {

  SAVE_APP_FORM("Save application form"),
  SAVE_APP_FORM_COMPLETE_LATER("Save application form and complete later"),

  APPLICATION_SEARCH("Application search"),
  CONSENT_SEARCH("Consent search"),

  SHOW_DIFFS_APP("Show differences between app and consents"),
  SHOW_DIFFS_PIPE("Show differences between pipeline versions"),
  SHOW_DIFFS_HUOO("Show differences between HUOO versions"),

  UPDATE_REQUEST_SENT("Update request sent"),

  APPLICATION_SUBMISSION("Application submitted"),

  APPLICATION_DELETED("Application deleted"),

  PAYMENT_ATTEMPT_STARTED("Payment attempt started"),
  PAYMENT_ATTEMPT_COMPLETED("Payment attempt completed"),
  PAYMENT_ATTEMPT_NOT_COMPLETED("Payment attempt not completed"),

  BACKGROUND_WORKAREA_TAB("Background work area tab accessed"),

  DOCUMENT_PREVIEW("Preview document");

  private final String displayName;

  AnalyticsEventCategory(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

}