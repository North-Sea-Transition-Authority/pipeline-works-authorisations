package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;

public enum SendConsentForApprovalRequirement {

  LATEST_APP_VERSION_IS_SATISFACTORY("The latest application version must be confirmed as satisfactory"),
  NO_UPDATE_IN_PROGRESS("There is an in progress update request"),
  NO_CONSULTATION_IN_PROGRESS("There is an open consultation"),
  NO_PUBLIC_NOTICE_IN_PROGRESS("There is an active public notice"),

  DOCUMENT_HAS_CLAUSES("The document must have clauses"),
  DOCUMENT_HAS_NO_MANUAL_MERGE_DATA("The document clauses must be edited to remove manual edit points"),

  MASTER_PWA_IS_NOT_CONSENTED("The PWA being varied is not yet consented");

  private final String defaultFailureReason;

  SendConsentForApprovalRequirement(String defaultFailureReason) {
    this.defaultFailureReason = defaultFailureReason;
  }

  public String getDefaultFailureReason() {
    return defaultFailureReason;
  }
}
