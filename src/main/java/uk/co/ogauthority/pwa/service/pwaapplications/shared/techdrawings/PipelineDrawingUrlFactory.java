package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings.PipelineDrawingController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class PipelineDrawingUrlFactory {

  private final PwaApplicationType applicationType;
  private final Integer applicationId;

  public PipelineDrawingUrlFactory(
      PwaApplicationType applicationType, Integer applicationId) {
    this.applicationType = applicationType;
    this.applicationId = applicationId;
  }

  public String getAddPipelineDrawingUrl() {
    return ReverseRouter.route(on(PipelineDrawingController.class)
        .renderAddDrawing(applicationType, applicationId, null, null));
  }

  public String getPipelineDrawingDownloadUrl(String fileId) {
    return ReverseRouter.route(on(PipelineDrawingController.class)
        .handleDownload(applicationType, applicationId, fileId, null));
  }
}
