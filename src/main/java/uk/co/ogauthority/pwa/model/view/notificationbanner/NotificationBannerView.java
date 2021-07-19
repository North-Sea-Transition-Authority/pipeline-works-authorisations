package uk.co.ogauthority.pwa.model.view.notificationbanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing an FDS notification banner that is viewed at the top of pages.
 * A notification banner's body can have multiple lines hence use of Builder pattern to add lines in fluent manner.
 */
public class NotificationBannerView {

  private final String title;
  private final List<NotificationBannerBodyLine> bodyLines;

  public static class BannerBuilder {
    private final String title;
    private final List<NotificationBannerBodyLine> bodyLines;

    public BannerBuilder(String title) {
      this.title = title;
      this.bodyLines = new ArrayList<>();
    }

    public BannerBuilder addBodyLine(NotificationBannerBodyLine notificationBannerBodyLine) {
      this.bodyLines.add(notificationBannerBodyLine);
      return this;
    }

    public NotificationBannerView build() {
      return new NotificationBannerView(this);
    }
  }

  private NotificationBannerView(BannerBuilder bannerBuilder) {
    this.title = bannerBuilder.title;
    this.bodyLines = bannerBuilder.bodyLines;
  }

  public String getTitle() {
    return title;
  }

  public List<NotificationBannerBodyLine> getBodyLines() {
    return bodyLines;
  }

}
