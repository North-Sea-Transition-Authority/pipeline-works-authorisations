package uk.co.ogauthority.pwa.service.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;

@Service
public class AsBuiltWorkareaPageService {

  @Autowired
  public AsBuiltWorkareaPageService() {
  }

  public PageView<PwaApplicationWorkAreaItem> getAsBuiltNotificationsPageView(int page) {

    var workAreaUri = ReverseRouter.route(
        on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.AS_BUILT_NOTIFICATIONS, page));

    return PageView.fromPage(
        Page.empty(),
        workAreaUri
    );

  }

}
