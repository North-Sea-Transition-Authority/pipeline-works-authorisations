package uk.co.ogauthority.pwa.model.view.notificationbanner;

import java.util.Objects;

/**
 * Class representing single line within the body of a notification banner.
 */
public class NotificationBannerBodyLine {

  private final String lineText;
  private final String lineClass;

  public NotificationBannerBodyLine(String lineText, String lineClass) {
    this.lineText = lineText;
    this.lineClass = lineClass;
  }

  public String getLineText() {
    return lineText;
  }

  public String getLineClass() {
    return lineClass;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotificationBannerBodyLine that = (NotificationBannerBodyLine) o;
    return Objects.equals(lineText, that.lineText) && Objects.equals(lineClass, that.lineClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lineText, lineClass);
  }
}
