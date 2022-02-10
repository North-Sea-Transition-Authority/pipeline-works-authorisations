package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.controller.PipelineDrawingController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class PipelineDrawingUrlFactory {

  private final PwaApplicationType applicationType;
  private final Integer applicationId;

  public PipelineDrawingUrlFactory(PwaApplicationDetail pwaApplicationDetail) {
    this.applicationType = pwaApplicationDetail.getPwaApplicationType();
    this.applicationId = pwaApplicationDetail.getMasterPwaApplicationId();
  }

  public String getAddPipelineDrawingUrl() {
    return ReverseRouter.route(on(PipelineDrawingController.class)
        .renderAddDrawing(applicationType, applicationId, null, null));
  }

  public String getPipelineDrawingDownloadUrl(String fileId) {
    return ReverseRouter.route(on(PipelineDrawingController.class)
        .handleDownload(applicationType, applicationId, fileId, null));
  }

  public String getPipelineDrawingRemoveUrl(Integer drawingId) {
    return ReverseRouter.route(on(PipelineDrawingController.class)
        .renderRemoveDrawing(applicationType, applicationId, drawingId, null));
  }

  public String getPipelineDrawingEditUrl(Integer drawingId) {
    return ReverseRouter.route(on(PipelineDrawingController.class)
    .renderEditDrawing(applicationType, applicationId, drawingId, null, null));
  }




  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineDrawingUrlFactory that = (PipelineDrawingUrlFactory) o;
    return applicationType == that.applicationType
        && Objects.equals(applicationId, that.applicationId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationType, applicationId);
  }
}
