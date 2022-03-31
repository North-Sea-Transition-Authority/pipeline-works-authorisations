package uk.co.ogauthority.pwa.service.footer;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

@Service
public class FooterService {

  private final List<FooterItem> footerItems;

  public FooterService() {
    this.footerItems = uk.co.ogauthority.pwa.features.webapp.footer.controller.FooterItem.stream()
        .map(FooterItem::from)
        .collect(Collectors.toList());
  }

  public void addFooterUrlsToModel(Model model) {
    model.addAttribute("footerItems", footerItems);
  }

  public void addFooterUrlsToModelAndView(ModelAndView modelAndView) {
    modelAndView.addObject("footerItems", footerItems);
  }

  public static class FooterItem {

    private final String displayName;

    private final String url;

    private final int displayOrder;

    public FooterItem(String displayName, String url, int displayOrder) {
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

    public static FooterItem from(uk.co.ogauthority.pwa.features.webapp.footer.controller.FooterItem footerItem) {
      return new FooterItem(footerItem.getDisplayName(), footerItem.getUrl(), footerItem.getDisplayOrder());
    }

  }

}
