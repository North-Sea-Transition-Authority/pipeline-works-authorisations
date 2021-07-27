package uk.co.ogauthority.pwa.model.form.asbuilt;

import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;

public class AsBuiltNotificationSubmissionForm {

  private AsBuiltNotificationStatus asBuiltNotificationStatus;
  private String perConsentDateWorkCompletedTimestampStr;
  private String notPerConsentDateWorkCompletedTimestampStr;
  private String notInConsentTimeframeDateWorkCompletedTimestampStr;
  private String perConsentDateBroughtIntoUseTimestampStr;
  private String notPerConsentDateBroughtIntoUseTimestampStr;
  private String ogaSubmissionReason;

  public AsBuiltNotificationSubmissionForm() {
  }

  public AsBuiltNotificationStatus getAsBuiltNotificationStatus() {
    return asBuiltNotificationStatus;
  }

  public void setAsBuiltNotificationStatus(AsBuiltNotificationStatus asBuiltNotificationStatus) {
    this.asBuiltNotificationStatus = asBuiltNotificationStatus;
  }

  public String getPerConsentDateWorkCompletedTimestampStr() {
    return perConsentDateWorkCompletedTimestampStr;
  }

  public void setPerConsentDateWorkCompletedTimestampStr(String perConsentDateWorkCompletedTimestampStr) {
    this.perConsentDateWorkCompletedTimestampStr = perConsentDateWorkCompletedTimestampStr;
  }

  public String getNotPerConsentDateWorkCompletedTimestampStr() {
    return notPerConsentDateWorkCompletedTimestampStr;
  }

  public void setNotPerConsentDateWorkCompletedTimestampStr(String notPerConsentDateWorkCompletedTimestampStr) {
    this.notPerConsentDateWorkCompletedTimestampStr = notPerConsentDateWorkCompletedTimestampStr;
  }

  public String getNotInConsentTimeframeDateWorkCompletedTimestampStr() {
    return notInConsentTimeframeDateWorkCompletedTimestampStr;
  }

  public void setNotInConsentTimeframeDateWorkCompletedTimestampStr(String notInConsentTimeframeDateWorkCompletedTimestampStr) {
    this.notInConsentTimeframeDateWorkCompletedTimestampStr = notInConsentTimeframeDateWorkCompletedTimestampStr;
  }

  public String getPerConsentDateBroughtIntoUseTimestampStr() {
    return perConsentDateBroughtIntoUseTimestampStr;
  }

  public void setPerConsentDateBroughtIntoUseTimestampStr(String perConsentDateBroughtIntoUseTimestampStr) {
    this.perConsentDateBroughtIntoUseTimestampStr = perConsentDateBroughtIntoUseTimestampStr;
  }

  public String getNotPerConsentDateBroughtIntoUseTimestampStr() {
    return notPerConsentDateBroughtIntoUseTimestampStr;
  }

  public void setNotPerConsentDateBroughtIntoUseTimestampStr(String notPerConsentDateBroughtIntoUseTimestampStr) {
    this.notPerConsentDateBroughtIntoUseTimestampStr = notPerConsentDateBroughtIntoUseTimestampStr;
  }

  public String getOgaSubmissionReason() {
    return ogaSubmissionReason;
  }

  public void setOgaSubmissionReason(String ogaSubmissionReason) {
    this.ogaSubmissionReason = ogaSubmissionReason;
  }

}
