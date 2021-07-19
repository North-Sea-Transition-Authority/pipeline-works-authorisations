package uk.co.ogauthority.pwa.model.enums.appprocessing;

public enum DefaultNotificationBannerType {
  PARALLEL_APPS_WARNING("You have submitted the following applications for the same PWA:", "If this application is planned to take place " +
      "first and it makes changes relevant to a submitted application you must contact the OGA to advise. If a submitted application is " +
      "planned to take place first and it makes changes relevant to this application then you must include these changes in your" +
      " application.");

  private final String title;
  private final String defaultBodyText;

  DefaultNotificationBannerType(String title, String defaultBodyText) {
    this.title = title;
    this.defaultBodyText = defaultBodyText;
  }

  public String getTitle() {
    return title;
  }

  public String getDefaultBodyText() {
    return defaultBodyText;
  }

}
