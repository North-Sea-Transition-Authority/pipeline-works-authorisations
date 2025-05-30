package uk.co.ogauthority.pwa.features.application.tasks.crossings.cable;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.controller.CableCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.controller.CableCrossingDocumentsController;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementRestController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

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
        .renderRemoveCableCrossing(pwaApplicationType, applicationId, crossingId, null));
  }

  public String getAddDocumentsUrl() {
    return ReverseRouter.route(on(CableCrossingDocumentsController.class)
        .renderEditCableCrossingDocuments(pwaApplicationType, applicationId, null, null));
  }

  public String getFileDownloadUrl() {
    // file id is null to allow templates to construct url as needed
    return ReverseRouter.route(on(PadFileManagementRestController.class).download(applicationId, null));
  }

}
