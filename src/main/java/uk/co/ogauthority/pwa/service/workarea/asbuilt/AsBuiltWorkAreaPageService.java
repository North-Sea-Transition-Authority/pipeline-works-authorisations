package uk.co.ogauthority.pwa.service.workarea.asbuilt;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationDtoRepository;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

@Service
public class AsBuiltWorkAreaPageService {

  private final AsBuiltNotificationDtoRepository asBuiltNotificationDtoRepository;

  @Autowired
  public AsBuiltWorkAreaPageService(
      AsBuiltNotificationDtoRepository asBuiltNotificationDtoRepository) {
    this.asBuiltNotificationDtoRepository = asBuiltNotificationDtoRepository;
  }

  public PageView<AsBuiltNotificationWorkAreaItem> getAsBuiltNotificationsPageView(AuthenticatedUserAccount authenticatedUserAccount,
                                                                                   int page) {
    var workAreaUri = ReverseRouter.route(
        on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.AS_BUILT_NOTIFICATIONS, page, Optional.empty()));

    var asBuiltNotificationSearchResults = asBuiltNotificationDtoRepository
        .findAllAsBuiltNotificationsForUser(authenticatedUserAccount,
            WorkAreaUtils.getWorkAreaPageRequest(page, AsBuiltWorkAreaSort.DEADLINE_DATE_ASC));

    return PageView.fromPage(
        asBuiltNotificationSearchResults,
        workAreaUri,
        AsBuiltNotificationWorkAreaItem::new
    );

  }


}
