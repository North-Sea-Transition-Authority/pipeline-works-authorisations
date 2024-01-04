package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.controller.CarbonStorageAreaCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.controller.CarbonStorageAreaCrossingDocumentsController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.controller.CrossingAgreementsController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class CarbonStorageCrossingUrlFactory {

  private final PwaApplicationDetail pwaApplicationDetail;

  private final Class<CarbonStorageAreaCrossingController> controllerClass = CarbonStorageAreaCrossingController.class;

  public CarbonStorageCrossingUrlFactory(PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public String getOverviewCarbonStorageCrossingUrl() {
    return ReverseRouter.route(on(CrossingAgreementsController.class)
        .renderCrossingAgreementsOverview(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null,
            null));
  }

  public String getCarbonStorageCrossingDocumentsUrl() {
    return ReverseRouter.route(on(CarbonStorageAreaCrossingDocumentsController.class)
        .renderEditCrossingDocuments(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null,
            null));
  }

  public String getFileDownloadUrl() {
    return ReverseRouter.route(on(CarbonStorageAreaCrossingDocumentsController.class)
        .handleDownload(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null,
            null));
  }


  public String getAddCarbonStorageCrossingUrl() {
    return ReverseRouter.route(on(controllerClass)
        .renderAddAreaCrossing(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null,
            null));
  }

  public String getEditCarbonStorageCrossingUrl(int crossingId) {
    return ReverseRouter.route(on(controllerClass)
        .renderEditAreaCrossing(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            crossingId,
            null,
            null));
  }

  public String getRemoveCarbonStorageCrossingUrl(int crossingId) {
    return ReverseRouter.route(on(controllerClass)
        .renderRemoveAreaCrossing(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            crossingId,
            null));
  }
}
