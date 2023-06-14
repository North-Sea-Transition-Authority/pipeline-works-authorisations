package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.controller.BlockCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.controller.BlockCrossingDocumentsController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class BlockCrossingUrlFactory {

  private final PwaApplicationDetail pwaApplicationDetail;

  public BlockCrossingUrlFactory(PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }


  public String getAddBlockCrossingUrl() {
    return ReverseRouter.route(on(BlockCrossingController.class)
        .renderAddBlockCrossing(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null,
            null));
  }

  public String getBlockCrossingDocumentsUrl() {
    return ReverseRouter.route(on(BlockCrossingDocumentsController.class)

        .renderEditBlockCrossingDocuments(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null,
            null));
  }

  public String getFileDownloadUrl() {
    return ReverseRouter.route(on(BlockCrossingDocumentsController.class)
        // file id is full to allow templates to construct url as needed
        .handleDownload(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null,
            null));
  }

  public String getEditBlockCrossingUrl(int blockCrossingId) {
    return ReverseRouter.route(on(BlockCrossingController.class).renderEditBlockCrossing(
        pwaApplicationDetail.getPwaApplicationType(),
        pwaApplicationDetail.getMasterPwaApplicationId(),
        blockCrossingId,
        null,
        null
    ));
  }

  public String getRemoveBlockCrossingUrl(int blockCrossingId) {
    return ReverseRouter.route(on(BlockCrossingController.class).removeBlockCrossing(
        pwaApplicationDetail.getPwaApplicationType(),
        pwaApplicationDetail.getMasterPwaApplicationId(),
        blockCrossingId,
        null
    ));
  }
}
