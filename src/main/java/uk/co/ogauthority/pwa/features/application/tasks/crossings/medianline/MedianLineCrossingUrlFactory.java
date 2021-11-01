package uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.controller.MedianLineCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.controller.MedianLineDocumentsController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class MedianLineCrossingUrlFactory {

  private final int pwaApplicationId;
  private final PwaApplicationType applicationType;

  public MedianLineCrossingUrlFactory(PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationId = pwaApplicationDetail.getMasterPwaApplicationId();
    this.applicationType = pwaApplicationDetail.getPwaApplicationType();
  }

  public String getAddMedianLineCrossingUrl() {
    return ReverseRouter.route(on(MedianLineCrossingController.class)
        .renderMedianLineForm(applicationType, pwaApplicationId, null, null));
  }

  public String getMedianLineCrossingDocumentsUrl() {
    return ReverseRouter.route(on(MedianLineDocumentsController.class)
      .renderEditMedianLineCrossingDocuments(applicationType, pwaApplicationId, null, null));
  }

  public String getFileDownloadUrl() {
    return ReverseRouter.route(on(MedianLineDocumentsController.class)
        // file id is full to allow templates to construct url as needed
        .handleDownload(applicationType, pwaApplicationId, null, null));
  }

}
