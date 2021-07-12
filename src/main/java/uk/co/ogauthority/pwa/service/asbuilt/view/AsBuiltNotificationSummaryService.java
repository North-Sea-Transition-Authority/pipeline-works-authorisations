package uk.co.ogauthority.pwa.service.asbuilt.view;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.appsummary.ApplicationSummaryController;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltNotificationGroupSummaryView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
class AsBuiltNotificationSummaryService {

  private final PwaHolderService pwaHolderService;

  @Autowired
  AsBuiltNotificationSummaryService(
      PwaHolderService pwaHolderService) {
    this.pwaHolderService = pwaHolderService;
  }




  AsBuiltNotificationGroupSummaryView getAsBuiltNotificationGroupSummaryView(
      AsBuiltNotificationGroupDetail asBuiltNotificationGroupDetail) {
    var asBuiltNotificationGroup = asBuiltNotificationGroupDetail.getAsBuiltNotificationGroup();
    var pwaApplication = asBuiltNotificationGroup.getPwaConsent().getSourcePwaApplication();
    var appTypeDisplay = pwaApplication.getApplicationType().getDisplayName();
    var masterPwa = asBuiltNotificationGroup.getPwaConsent().getMasterPwa();
    var pwaReference = asBuiltNotificationGroup.getPwaConsent().getReference();
    var appReference = asBuiltNotificationGroup.getReference();
    var holders = pwaHolderService.getPwaHolders(masterPwa).stream()
        .map(PortalOrganisationGroup::getName).collect(Collectors.joining(", "));
    var deadline = DateUtils.formatDate(asBuiltNotificationGroupDetail.getDeadlineDate());
    var accessLink = ReverseRouter.route(on(ApplicationSummaryController.class).renderSummary(pwaApplication.getId(),
        pwaApplication.getApplicationType(), null, null, null, null));
    return new AsBuiltNotificationGroupSummaryView(appTypeDisplay, pwaReference, appReference, holders, deadline, accessLink);
  }

}
