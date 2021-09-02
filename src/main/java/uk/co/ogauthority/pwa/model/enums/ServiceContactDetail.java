package uk.co.ogauthority.pwa.model.enums;

public enum ServiceContactDetail {


  BUSINESS_SUPPORT(
      "Business support",
      "OGA",
      "consents@ogauthority.co.uk",
      "",
      "https://www.ogauthority.co.uk/licensing-consents/consents/pipeline-works-authorisations/",
      "For example, questions about filling in your application and the information you need to provide",
      true,
      10),

  TECHNICAL_SUPPORT(
      "Technical support",
      "UKOP service desk",
      "ukop@ogauthority.co.uk",
      "0300 067 1682",
      "",
      "For example, unexpected problems using the service or system errors being received",
      true,
      20);

  private final String displayName;

  private final String serviceName;

  private final String emailAddress;

  private final String phoneNumber;

  private final String guidanceUrl;

  private final String description;

  private final boolean shownOnContactPage;

  private final int displayOrder;

  ServiceContactDetail(String displayName,
                       String serviceName,
                       String emailAddress,
                       String phoneNumber,
                       String guidanceUrl,
                       String description,
                       boolean shownOnContactPage,
                       int displayOrder) {
    this.displayName = displayName;
    this.serviceName = serviceName;
    this.emailAddress = emailAddress;
    this.phoneNumber = phoneNumber;
    this.guidanceUrl = guidanceUrl;
    this.description = description;
    this.shownOnContactPage = shownOnContactPage;
    this.displayOrder = displayOrder;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getGuidanceUrl() {
    return guidanceUrl;
  }

  public String getDescription() {
    return description;
  }

  public boolean isShownOnContactPage() {
    return shownOnContactPage;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }
}
