package uk.co.ogauthority.pwa.model.form.asbuilt;

import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;

public class AsBuiltNotificationSubmissionForm {

  private AsBuiltNotificationStatus asBuiltNotificationStatus;
  private String perConsentDateLaidTimestampStr;
  private String notPerConsentDateLaidTimestampStr;
  private String notInConsentTimeframeDateLaidTimestampStr;
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

  public String getPerConsentDateLaidTimestampStr() {
    return perConsentDateLaidTimestampStr;
  }

  public void setPerConsentDateLaidTimestampStr(String perConsentDateLaidTimestampStr) {
    this.perConsentDateLaidTimestampStr = perConsentDateLaidTimestampStr;
  }

  public String getNotPerConsentDateLaidTimestampStr() {
    return notPerConsentDateLaidTimestampStr;
  }

  public void setNotPerConsentDateLaidTimestampStr(String notPerConsentDateLaidTimestampStr) {
    this.notPerConsentDateLaidTimestampStr = notPerConsentDateLaidTimestampStr;
  }

  public String getNotInConsentTimeframeDateLaidTimestampStr() {
    return notInConsentTimeframeDateLaidTimestampStr;
  }

  public void setNotInConsentTimeframeDateLaidTimestampStr(String notInConsentTimeframeDateLaidTimestampStr) {
    this.notInConsentTimeframeDateLaidTimestampStr = notInConsentTimeframeDateLaidTimestampStr;
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
