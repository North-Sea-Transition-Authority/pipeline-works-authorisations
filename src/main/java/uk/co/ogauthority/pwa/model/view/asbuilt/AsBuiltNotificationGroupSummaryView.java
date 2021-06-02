package uk.co.ogauthority.pwa.model.view.asbuilt;

/**
 * A class to contains user displayable info about an individual as-built notification group.
 */
public class AsBuiltNotificationGroupSummaryView {

  private final String applicationTypeDisplay;
  private final String consentReference;
  private final String appReference;
  private final String holder;
  private final String asBuiltDeadline;
  private final String accessLink;

  public AsBuiltNotificationGroupSummaryView(
      String applicationTypeDisplay, String consentReference,
      String appReference, String holder, String asBuiltDeadline,
      String accessLink) {
    this.applicationTypeDisplay = applicationTypeDisplay;
    this.consentReference = consentReference;
    this.appReference = appReference;
    this.holder = holder;
    this.asBuiltDeadline = asBuiltDeadline;
    this.accessLink = accessLink;
  }

  public String getConsentReference() {
    return consentReference;
  }

  public String getAppReference() {
    return appReference;
  }

  public String getHolder() {
    return holder;
  }

  public String getAsBuiltDeadline() {
    return asBuiltDeadline;
  }

  public String getAccessLink() {
    return accessLink;
  }

  public String getApplicationTypeDisplay() {
    return applicationTypeDisplay;
  }

}
