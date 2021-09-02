package uk.co.ogauthority.pwa.service.footer;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.util.ControllerUtils;

@Service
public class FooterService {

  private static final String ACCESSIBILITY_STATEMENT_URL_ATTR_NAME = "accessibilityStatementUrl";
  private static final String CONTACT_INFORMATION_URL_ATTR_NAME = "contactInformationUrl";

  public void addFooterUrlsToModel(Model model) {
    getFooterItems().forEach(footerItem -> model.addAttribute(footerItem.getAttributeName(), footerItem.getUrl()));
  }

  public void addFooterUrlsToModelAndView(ModelAndView modelAndView) {
    getFooterItems().forEach(footerItem -> modelAndView.addObject(footerItem.getAttributeName(), footerItem.getUrl()));
  }

  private List<FooterItem> getFooterItems() {
    var footerItems = new ArrayList<FooterItem>();

    footerItems.add(new FooterItem(
        ACCESSIBILITY_STATEMENT_URL_ATTR_NAME,
        ControllerUtils.getAccessibilityStatementUrl()));

    footerItems.add(new FooterItem(
        CONTACT_INFORMATION_URL_ATTR_NAME,
        ControllerUtils.getContactInformationUrl()));

    return footerItems;
  }

  private static class FooterItem {

    private final String attributeName;

    private final String url;

    FooterItem(String attributeName, String url) {
      this.attributeName = attributeName;
      this.url = url;
    }

    String getAttributeName() {
      return attributeName;
    }

    String getUrl() {
      return url;
    }
  }
}
