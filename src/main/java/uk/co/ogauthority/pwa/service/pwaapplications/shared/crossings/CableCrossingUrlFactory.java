package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.CableCrossingController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class CableCrossingUrlFactory {

  private PwaApplicationType pwaApplicationType;
  private Integer applicationId;

  public CableCrossingUrlFactory(PwaApplicationDetail pwaApplicationDetail) {
    pwaApplicationType = pwaApplicationDetail.getPwaApplicationType();
    applicationId = pwaApplicationDetail.getMasterPwaApplicationId();
  }

  public String getAddCableCrossingUrl() {
    return ReverseRouter.route(on(CableCrossingController.class)
        .renderAddCableCrossing(pwaApplicationType, applicationId, null, null));
  }

  public String getEditCableCrossingUrl(Integer crossingId) {
    return ReverseRouter.route(on(CableCrossingController.class)
        .renderEditCableCrossing(pwaApplicationType, applicationId, crossingId, null, null));
  }

  public String getRemoveCableCrossingUrl(Integer crossingId) {
    return ReverseRouter.route(on(CableCrossingController.class)
        .postRemoveCableCrossing(pwaApplicationType, applicationId, crossingId, null));
  }

}
