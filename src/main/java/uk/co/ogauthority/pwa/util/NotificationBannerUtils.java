package uk.co.ogauthority.pwa.util;

import java.util.List;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.view.notificationbanner.NotificationBannerBodyLine;
import uk.co.ogauthority.pwa.model.view.notificationbanner.NotificationBannerView;

public class NotificationBannerUtils {

  public static void infoBanner(String title, List<NotificationBannerBodyLine> bodyLines, ModelAndView modelAndView) {
    var notificationBannerViewBuilder = new NotificationBannerView.BannerBuilder(title);
    bodyLines.forEach(notificationBannerViewBuilder::addBodyLine);
    var notificationBannerView = notificationBannerViewBuilder.build();

    modelAndView.addObject("notificationBannerView", notificationBannerView);
  }
}
