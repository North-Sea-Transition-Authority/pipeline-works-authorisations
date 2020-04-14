package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.BlockCrossingController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.BlockCrossingDocumentsController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class BlockCrossingUrlFactory {

  private final int pwaApplicationId;
  private final PwaApplicationType applicationType;

  public BlockCrossingUrlFactory(PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationId = pwaApplicationDetail.getMasterPwaApplicationId();
    this.applicationType = pwaApplicationDetail.getPwaApplicationType();
  }


  public String getAddBlockCrossingUrl() {
    return ReverseRouter.route(on(BlockCrossingController.class)
        .renderAddBlockCrossing(applicationType, pwaApplicationId, null, null));
  }

  public String getBlockCrossingDocumentsUrl() {
    return ReverseRouter.route(on(BlockCrossingDocumentsController.class)

        .renderEditBlockCrossingDocuments(applicationType, pwaApplicationId, null, null));
  }

  public String getFileDownloadUrl() {
    return ReverseRouter.route(on(BlockCrossingDocumentsController.class)
        // file id is full to allow templates to construct url as needed
        .handleDownload(applicationType, pwaApplicationId, null, null));
  }

  public String getEditBlockCrossingUrl(int blockCrossingId) {
    return ReverseRouter.route(on(BlockCrossingController.class).renderEditBlockCrossing(
        applicationType,
        pwaApplicationId,
        blockCrossingId,
        null,
        null
    ));
  }

  public String getRemoveBlockCrossingUrl(int blockCrossingId) {
    return ReverseRouter.route(on(BlockCrossingController.class).removeBlockCrossing(
        applicationType,
        pwaApplicationId,
        blockCrossingId,
        null
    ));
  }
}
