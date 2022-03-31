package uk.co.ogauthority.pwa.service.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class WorkAreaTabUrlFactory {

  public WorkAreaTabUrlFactory() {
  }

  public String getTabUrl(String tabValue) {

    var workAreaTab = WorkAreaTab.fromValue(tabValue);

    return ReverseRouter.route(on(WorkAreaController.class).renderWorkAreaTab(null, workAreaTab, null, Optional.empty()));

  }

}
