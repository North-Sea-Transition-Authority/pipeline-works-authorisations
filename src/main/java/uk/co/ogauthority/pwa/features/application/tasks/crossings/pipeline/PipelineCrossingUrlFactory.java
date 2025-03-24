package uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.controller.PipelineCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.controller.PipelineCrossingDocumentsController;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementRestController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

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

  public String getEditCrossingUrl(Integer crossingId) {
    return ReverseRouter.route(on(PipelineCrossingController.class)
        .renderEditCrossing(applicationType, applicationId, crossingId, null, null));
  }

  public String getRemoveCrossingUrl(Integer crossingId) {
    return ReverseRouter.route(on(PipelineCrossingController.class)
        .renderRemoveCrossing(applicationType, applicationId, crossingId, null));
  }

  public String getAddDocumentsUrl() {
    return ReverseRouter.route(on(PipelineCrossingDocumentsController.class)
        .renderEditPipelineCrossingDocuments(applicationType, applicationId, null, null));
  }

  public String getFileDownloadUrl() {
    // file id is null to allow templates to construct url as needed
    return ReverseRouter.route(on(PadFileManagementRestController.class).download(applicationId, null));
  }
}
