package uk.co.ogauthority.pwa.testutils;

import java.util.List;
import uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

public class ApplicationSummaryViewTestUtils {

  private ApplicationSummaryViewTestUtils() {
    throw new AssertionError();
  }

  public static ApplicationSummaryView getView() {

    String html = "<html>";
    List<SidebarSectionLink> links = List.of();

    return new ApplicationSummaryView(html, links);

  }

}
