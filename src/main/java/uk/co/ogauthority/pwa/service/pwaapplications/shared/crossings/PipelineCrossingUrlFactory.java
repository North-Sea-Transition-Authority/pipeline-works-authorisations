package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.PipelineCrossingController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class PipelineCrossingUrlFactory {

  private PwaApplicationType applicationType;
  private Integer applicationId;

  public PipelineCrossingUrlFactory(PwaApplicationDetail pwaApplicationDetail) {
    applicationType = pwaApplicationDetail.getPwaApplicationType();
    applicationId = pwaApplicationDetail.getMasterPwaApplicationId();
  }

  public String getAddCrossingUrl() {
    return ReverseRouter.route(on(PipelineCrossingController.class)
        .renderAddCrossing(applicationType, applicationId, null, null));
  }
}
