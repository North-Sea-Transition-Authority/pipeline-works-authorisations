package uk.co.ogauthority.pwa.features.webapp.footer.controller;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.util.ControllerUtils;

public enum FooterItem {

  ACCESSIBILITY_STATEMENT(
      "Accessibility statement",
      ControllerUtils.getAccessibilityStatementUrl(),
      10
  ),

  CONTACT_INFORMATION(
      "Contact",
      ControllerUtils.getContactInformationUrl(),
      20
  ),

  COOKIES(
      "Cookies",
      ControllerUtils.getCookiesUrl(),
      30
  ),

  FEEDBACK(
      "Feedback",
      ControllerUtils.getFeedbackUrl(),
      40
  );

  private final String displayName;
  private final String url;
  private final int displayOrder;

  FooterItem(String displayName, String url, int displayOrder) {
    this.displayName = displayName;
    this.url = url;
    this.displayOrder = displayOrder;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUrl() {
    return url;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<FooterItem> stream() {
    return Stream.of(FooterItem.values());
  }

}
