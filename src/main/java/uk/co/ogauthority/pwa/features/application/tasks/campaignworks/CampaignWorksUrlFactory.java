package uk.co.ogauthority.pwa.features.application.tasks.campaignworks;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.controller.CampaignWorksController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class CampaignWorksUrlFactory {

  private final int applicationId;
  private final PwaApplicationType applicationType;

  public CampaignWorksUrlFactory(PwaApplicationDetail pwaApplicationDetail) {
    this.applicationId = pwaApplicationDetail.getMasterPwaApplicationId();
    this.applicationType = pwaApplicationDetail.getPwaApplicationType();
  }


  public String addWorkScheduleUrl() {
    return ReverseRouter.route(on(CampaignWorksController.class)
        .renderAddWorkSchedule(this.applicationType, this.applicationId, null, null));
  }

  public String editWorkScheduleUrl(int campaignWorksId) {
    return ReverseRouter.route(on(CampaignWorksController.class).renderEditWorkSchedule(
        this.applicationType,
        this.applicationId,
        campaignWorksId,
        null,
        null));
  }

  public String removeWorkScheduleUrl(int campaignWorksId) {
    return ReverseRouter.route(on(CampaignWorksController.class).renderRemoveWorkSchedule(
        this.applicationType,
        this.applicationId,
        campaignWorksId,
        null));
  }
}
